package io.github.wcwac.beecount.capture

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import io.github.wcwac.beecount.capture.rules.*
import io.github.wcwac.beecount.domain.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import java.lang.ref.WeakReference
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/** Read-only view of the service configuration currently registered with Android. */
data class CaptureConnectionInfo(
    val connected: Boolean = false,
    val ready: Boolean = false,
    val eventTypes: Int = 0,
    val packages: Set<String> = emptySet(),
    val flags: Int = 0,
    val canRetrieveWindowContent: Boolean = false,
    val toolIdentity: Boolean = false,
)

/** A native capture pipeline whose lifetime never depends on Activity or Compose. */
class NativeCaptureService : AccessibilityService() {
    private lateinit var host: CaptureHost
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val queue = CaptureQueue()
    private val registry = RuleRegistry()
    private val callbacks = ConcurrentHashMap<String, RuleCallback>()
    private val lastCandidates = mutableMapOf<String, Pair<String, String>>()
    private val drainWaiters = ConcurrentHashMap.newKeySet<CompletableDeferred<Unit>>()
    private val generation = AtomicLong()
    @Volatile private var restorePaused = false
    @Volatile private var ready = false
    private val session = UUID.randomUUID().toString()
    private val main = Handler(Looper.getMainLooper())
    @Volatile private var status = CaptureStatus()
    private var configJob: Job? = null
    private var inboxJob: Job? = null
    private var observedHostGeneration = Long.MIN_VALUE
    private var observedServiceGeneration = Long.MIN_VALUE
    private val statusPending = java.util.concurrent.atomic.AtomicBoolean()
    private val publishStatus = Runnable { statusPending.set(false); if (::host.isInitialized) host.onCaptureStatus(status) }

    override fun onCreate() {
        super.onCreate()
        host = (application as CaptureHostProvider).captureHost
    }
    override fun onServiceConnected() {
        connected = WeakReference(this)
        update { copy(connected = true, stage = "INITIALIZING", lastResult = "INITIALIZING") }
        configJob?.cancel(); configJob = scope.launch {
            try { host.awaitReady() } catch (failure: Exception) {
                if (failure is CancellationException) throw failure
                update { copy(lastResult = "INITIALIZATION_FAILED", failure = failure.javaClass.simpleName) }; return@launch
            }
            if (connected.get() !== this@NativeCaptureService) return@launch
            ready = true
            host.captureConfig.collectLatest { config ->
            val before = serviceInfo?.eventTypes ?: 0
            applyConfig(config)
            if (!config.enabled && before != 0) invalidate()
        } }
        inboxJob?.cancel(); inboxJob = scope.launch {
            try { host.awaitReady() } catch (failure: Exception) { if (failure is CancellationException) throw failure; return@launch }
            host.captureInbox.observePending().collect { pending ->
            val ids = pending.mapTo(hashSetOf()) { it.id }
            callbacks.keys.filter { it !in ids }.forEach(::handled)
        } }
    }
    private fun applyConfig(config: CaptureConfig) {
        val info = serviceInfo ?: return
        val packages = config.selectedPackages.intersect(CaptureApps.packages)
        info.packageNames = packages.toTypedArray()
        info.eventTypes = if (config.enabled && packages.isNotEmpty()) AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED else 0
        info.flags = AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS or AccessibilityServiceInfo.FLAG_REQUEST_ENHANCED_WEB_ACCESSIBILITY or AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS or AccessibilityServiceInfo.DEFAULT
        info.notificationTimeout = 10
        serviceInfo = info
        update { copy(effectivePackages = packages, stage = "IDLE", lastResult = if (config.enabled) "LISTENING" else "DISABLED") }
    }
    private fun accepts(pkg: String) = ready && !restorePaused && host.captureConfig.value.let { it.enabled && pkg in it.selectedPackages && pkg in CaptureApps.packages }
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val pkg = event?.packageName?.toString() ?: return
        if (!accepts(pkg)) return
        val page = event.className?.toString().orEmpty()
        val received = SystemClock.elapsedRealtime()
        update { copy(events = events + 1, lastPackage = pkg, lastPage = page, lastEventAt = System.currentTimeMillis()) }
        val source = try { event.source } catch (failure: RuntimeException) { update { copy(failure = failure.javaClass.simpleName, lastResult = "EVENT_SOURCE_FAILED") }; null }
        val result = queue.offer(Event(pkg, page, event.eventType, event.windowId, received, generation.get(), host.captureGeneration, source))
        if (!result.accepted) update { copy(skipped = skipped + 1, lastResult = "QUEUE_FULL") }
    }
    private inner class Event(
        pkg: String, private val page: String, eventType: Int, private val window: Int,
        private val received: Long, private val epoch: Long, private val hostEpoch: Long,
        private var owned: AccessibilityNodeInfo?
    ) : CaptureEventTask(pkg, eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
        private fun current() = connected.get() === this@NativeCaptureService && epoch == generation.get() && hostEpoch == host.captureGeneration && accepts(packageName)
        override fun process() {
            if (!current()) return
            if (observedHostGeneration != hostEpoch || observedServiceGeneration != epoch) { registry.reset(); callbacks.clear(); lastCandidates.clear(); observedHostGeneration = hostEpoch; observedServiceGeneration = epoch }
            update { copy(stage = "RESOLVING_SOURCE") }
            try {
                val initial = owned; owned = null
                val resolved = SourceResolver.resolve(this@NativeCaptureService, initial, packageName, window, SystemClock.elapsedRealtime() - received)
                owned = resolved.node
                if (!current()) return
                val context = RuleContext(host.captureConfig.value.autoSave) { update { copy(reads = reads + 1, stage = "PARSING") } }
                val arena = NodeArena()
                try {
                    val node = arena.own(owned); owned = null
                    RuleBridge.context.set(context)
                    registry.accept(context, packageName, page, node)
                } finally { RuleBridge.context.remove(); arena.close() }
                if (!current()) return
                context.enrichment?.let { remark ->
                    val candidateId = lastCandidates[packageName]
                    if (candidateId != null) main.postDelayed({
                        if (current()) queue.execute {
                            if (current()) try { runBlocking { host.captureInbox.enrichRemark(candidateId.first, remark.take(2000), candidateId.second) } }
                            catch (failure: Exception) { update { copy(lastResult = "ENRICH_FAILED", failure = failure.javaClass.simpleName) } }
                        }
                    }, 1000)
                }
                if (context.submissions.isEmpty()) update { copy(lastResult = if (resolved.node == null) resolved.result else if (context.tokens.isEmpty()) "PAGE_GATE_NOT_MATCHED" else "NO_MATCH") }
                for ((bill, callback) in context.submissions) {
                    if (!current()) break
                    val proposal = proposal(bill, context, packageName, page, window, hostEpoch, epoch, resolved.route) ?: continue
                    update { copy(proposals = proposals + 1, stage = "PERSISTING") }
                    val result = runBlocking { host.captureInbox.upsert(proposal) }
                    if (!current()) break
                    val candidate = result.candidate
                    if (candidate != null && candidate.state == CandidateState.PENDING) {
                        RuleBridge.f1838j = System.currentTimeMillis()
                        lastCandidates[packageName] = candidate.id to candidate.proposal.description
                        if (callback != null) callbacks.putIfAbsent(candidate.id, callback)
                        update { copy(candidates = candidates + if (result.kind == CaptureUpsertKind.CREATED) 1 else 0, lastResult = "CANDIDATE_${result.kind}") }
                        if (result.kind in setOf(CaptureUpsertKind.CREATED, CaptureUpsertKind.UPDATED, CaptureUpsertKind.CONFLICT)) main.post {
                            if (current()) host.onCandidateAvailable(candidate)
                        }
                    } else { callback?.dismiss(); update { copy(lastResult = "CAPTURE_${result.kind}") } }
                }
            } catch (aborted: ReadAborted) { update { copy(lastResult = "READ_${aborted.reason}", failure = aborted.reason) } }
            catch (failure: Exception) { update { copy(lastResult = "CAPTURE_FAILED", failure = failure.javaClass.simpleName) } }
            finally { update { copy(stage = "IDLE") } }
        }
        override fun closeResource() { val node = owned; owned = null; SourceResolver.release(node) }
    }
    private fun proposal(bill: RuleBill, context: RuleContext, pkg: String, page: String, window: Int, epoch: Long, serviceEpoch: Long, route: String): CaptureProposal? {
        val amount = try {
            val raw = bill.getNumber().orEmpty().trim()
            require(raw.length <= 128)
            val text = raw.replace(Regex("^([+-]?)[¥￥]")) { it.groupValues[1] }.removeSuffix("元").trim()
            require(text.matches(Regex("[+-]?(?:[0-9]+|[0-9]{1,3}(?:,[0-9]{3})+)(?:\\.[0-9]+)?(?:[eE][+-]?[0-9]+)?")))
            val decimal = BigDecimal(text.replace(",", "")).abs().stripTrailingZeros()
            // The original rules can emit Double-calculated text. Never add another binary round-trip or silently round it here.
            require(decimal.scale() <= CurrencyCode.CNY.scale)
            require(decimal <= BigDecimal.valueOf(Long.MAX_VALUE, CurrencyCode.CNY.scale))
            Money.parse(decimal.toPlainString())
        } catch (_: RuntimeException) { update { copy(lastResult = "INVALID_AMOUNT") }; return null }
        if (amount.minor <= 0) { update { copy(lastResult = "INVALID_AMOUNT") }; return null }
        val kind = if (bill.isTransfer()) EntryKind.TRANSFER else if (bill.isIncome()) EntryKind.INCOME else EntryKind.EXPENSE
        val identityMerchant = bill.getShopName().orEmpty().take(2000)
        val merchant = identityMerchant.take(500)
        val method = if (bill.isTransfer()) bill.getFromAsset().orEmpty() + " -> " + bill.getToAsset().orEmpty() else bill.getAsset().orEmpty()
        val identity = CaptureIdentity.identify(pkg, "$session:$epoch:$serviceEpoch", window, kind, amount.minor, bill.getTime(), identityMerchant, method, context.tokens)
        fun minor(value: String?): Long = try { BigDecimal(value.orEmpty()).movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValueExact().coerceAtLeast(0) } catch (_: RuntimeException) { 0 }
        return CaptureProposal(
            packageName = pkg, pageClass = page, sessionId = "$session:$epoch:$serviceEpoch", windowId = window, kind = kind,
            amount = amount, merchant = merchant, description = bill.getRemark().orEmpty().take(2000), paymentMethod = bill.getAsset().orEmpty().take(2000),
            occurredAt = bill.getTime().takeIf { it > 0 } ?: System.currentTimeMillis(), visibleTransactionTime = identity.visibleTime,
            paymentId = identity.paymentId, orderReference = identity.orderReference, identityKind = identity.kind,
            strongKey = identity.strongKey, windowKey = identity.windowKey, requiresConfirmation = identity.strongKey == null,
            fromPaymentMethod = bill.getFromAsset().orEmpty().take(2000), toPaymentMethod = bill.getToAsset().orEmpty().take(2000),
            feeMinor = minor(bill.getServiceCharge().takeIf { it.isFinite() }?.let { BigDecimal.valueOf(it).toPlainString() }), discountMinor = minor(bill.getDiscount()), sourceRoute = route
        )
    }
    @Synchronized private fun update(change: CaptureStatus.() -> CaptureStatus) {
        status = status.change()
        if (statusPending.compareAndSet(false, true)) main.postDelayed(publishStatus, 200)
    }
    private fun handled(id: String) {
        if (!queue.execute { callbacks.remove(id)?.dismiss(); RuleBridge.f1838j = System.currentTimeMillis() }) update { copy(skipped = skipped + 1, lastResult = "CALLBACK_QUEUE_FULL") }
    }
    private fun invalidate() {
        generation.incrementAndGet(); queue.discardPending()
        if (!queue.execute { registry.reset(); callbacks.clear(); lastCandidates.clear() }) update { copy(lastResult = "RESET_QUEUE_FULL") }
    }
    override fun onInterrupt() { update { copy(lastResult = "FEEDBACK_INTERRUPTED") } }
    override fun onUnbind(intent: Intent?): Boolean { disconnect(); return super.onUnbind(intent) }
    override fun onDestroy() { disconnect(); scope.cancel(); queue.close(); drainWaiters.forEach { it.completeExceptionally(IllegalStateException("采集服务已断开，请重试恢复")) }; drainWaiters.clear(); main.removeCallbacks(publishStatus); statusPending.set(false); if (::host.isInitialized) host.onCaptureStatus(status); super.onDestroy() }
    private fun disconnect() {
        if (connected.get() === this) connected.clear()
        ready = false
        generation.incrementAndGet(); queue.discardPending(); configJob?.cancel(); inboxJob?.cancel()
        update { copy(connected = false, stage = "IDLE", lastResult = "DISCONNECTED") }
    }
    companion object {
        @Volatile private var connected = WeakReference<NativeCaptureService>(null)
        fun isConnected(): Boolean = connected.get() != null
        /** Queries actual Android registration, independently of the persisted master switch. */
        fun connectionInfo(): CaptureConnectionInfo {
            val service = connected.get() ?: return CaptureConnectionInfo()
            val info = try { service.serviceInfo } catch (_: RuntimeException) { null }
            return CaptureConnectionInfo(
                connected = true, ready = service.ready && !service.restorePaused,
                eventTypes = info?.eventTypes ?: 0, packages = info?.packageNames?.toSet().orEmpty(), flags = info?.flags ?: 0,
                canRetrieveWindowContent = info != null && (info.capabilities and AccessibilityServiceInfo.CAPABILITY_CAN_RETRIEVE_WINDOW_CONTENT) != 0,
                toolIdentity = Build.VERSION.SDK_INT >= 31 && info?.isAccessibilityTool == true,
            )
        }
        fun candidateHandled(candidateId: String) { connected.get()?.handled(candidateId) }
        fun invalidateForRestore() { connected.get()?.let { it.restorePaused = true; it.invalidate() } }
        fun resumeAfterRestore() { connected.get()?.let { it.generation.incrementAndGet(); it.restorePaused = false } }
        /** Await this before replacing the database so an in-flight upsert cannot race restoration. */
        suspend fun invalidateAndDrain() {
            val service = connected.get() ?: return
            service.restorePaused = true
            service.generation.incrementAndGet(); service.queue.discardPending()
            val drained = CompletableDeferred<Unit>()
            service.drainWaiters.add(drained)
            try {
            check(service.queue.execute { service.registry.reset(); service.callbacks.clear(); service.lastCandidates.clear(); drained.complete(Unit) }) { "采集队列繁忙，请稍后再恢复" }
            withTimeout(10_000) { drained.await() }
            } finally { service.drainWaiters.remove(drained) }
        }
    }
}
