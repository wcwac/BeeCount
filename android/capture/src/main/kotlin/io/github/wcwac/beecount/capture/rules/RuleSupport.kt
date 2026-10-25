@file:Suppress("UNUSED_PARAMETER", "UNCHECKED_CAST")
package io.github.wcwac.beecount.capture.rules

import android.os.Build
import android.os.SystemClock
import android.view.accessibility.AccessibilityNodeInfo
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.IdentityHashMap
import java.util.Locale
import kotlin.text.replace as replaceLiteral

internal interface RuleCallback { fun dismiss() }
internal class RuleContext(val autoSave: Boolean, val onRead: (Int) -> Unit) {
    val submissions = mutableListOf<Pair<RuleBill, RuleCallback?>>()
    var tokens: List<String> = emptyList()
    var enrichment: String? = null
}
internal object RuleBridge {
    var f1838j: Long = 0
    val context = ThreadLocal<RuleContext>()
    fun l(context: RuleContext?, bill: RuleBill?, callback: RuleCallback?) { if (context != null && bill != null) context.submissions += bill to callback }
    fun E(context: RuleContext?, remark: String?) { if (!remark.isNullOrEmpty()) context?.enrichment = remark }
}
internal object RuleSettings { fun i(): Boolean = RuleBridge.context.get()?.autoSave == true }
// Per-source user tags are resolved by the domain enrichment layer, not original numeric IDs.
internal object RuleTags { fun d(source: String?): MutableList<Long> = mutableListOf() }
// Original diagnostic calls never log payment text in the offline app.
internal object RuleLog { fun l(vararg ignored: Any?) {} ; fun a(vararg ignored: Any?) {} }
internal object Amounts {
    fun J(value: String?): Boolean = !value.isNullOrEmpty() && value.toDoubleOrNull() != null
    fun S(value: String?): Double = value?.replaceLiteral(',', '.')?.toDoubleOrNull() ?: 0.0
    fun t(value: Double): String {
        val rounded = DecimalFormat("0.00").apply { roundingMode = RoundingMode.HALF_UP }.format(value)
        if (value == 0.0 || S(rounded) == 0.0) return "0.00"
        val text = DecimalFormat("#.00").apply { roundingMode = RoundingMode.HALF_UP }.format(value)
        return when { text.startsWith(".") -> "0$text"; text.startsWith("-.") -> "-0" + text.substring(1); else -> text }
    }
}
internal object Dates {
    fun X0(text: String?, pattern: String?): Long {
        if (text == null || pattern == null) return -1
        return try { SimpleDateFormat(pattern, Locale.CHINA).parse(text, ParsePosition(0))?.time ?: -1 } catch (_: IllegalArgumentException) { -1 }
    }
    fun m0(first: Long, second: Long): Boolean {
        val a = Calendar.getInstance().apply { timeInMillis = first }
        val b = Calendar.getInstance().apply { timeInMillis = second }
        return intArrayOf(Calendar.ERA, Calendar.YEAR, Calendar.DAY_OF_YEAR).all { a[it] == b[it] }
    }
}

/** Owned nodes are scoped to one observation, including original find-by-text/view-ID gates. */
internal class NodeArena : AutoCloseable {
    private val nodes = java.util.Collections.newSetFromMap(IdentityHashMap<AccessibilityNodeInfo, Boolean>())
    fun own(node: AccessibilityNodeInfo?): CaptureNode? {
        if (node == null) return null
        if (nodes.size >= 4096 && !nodes.contains(node)) { recycle(node); throw ReadAborted("NODE_LIMIT") }
        nodes.add(node); return CaptureNode(node, this)
    }
    fun release(node: AccessibilityNodeInfo) { if (nodes.remove(node)) recycle(node) }
    fun releaseUnowned(node: AccessibilityNodeInfo) { if (!nodes.contains(node)) recycle(node) }
    override fun close() { nodes.forEach(::recycle); nodes.clear() }
    private fun recycle(node: AccessibilityNodeInfo) { if (Build.VERSION.SDK_INT < 33) try { @Suppress("DEPRECATION") node.recycle() } catch (_: RuntimeException) {} }
}
internal class CaptureNode(val raw: AccessibilityNodeInfo, private val arena: NodeArena) {
    var branches: Int = 0
        private set
    fun findAccessibilityNodeInfosByText(text: String?): MutableList<CaptureNode>? = find { raw.findAccessibilityNodeInfosByText(text.orEmpty()) }
    fun findAccessibilityNodeInfosByViewId(id: String?): MutableList<CaptureNode>? = find { raw.findAccessibilityNodeInfosByViewId(id.orEmpty()) }
    private fun find(query: () -> List<AccessibilityNodeInfo>?): MutableList<CaptureNode> {
        val result = query().orEmpty()
        val wrapped = mutableListOf<CaptureNode>(); var adopted = 0
        try { for (node in result) { adopted++; arena.own(node)?.let(wrapped::add) }; return wrapped }
        finally { for (index in adopted until result.size) arena.releaseUnowned(result[index]) }
    }
    fun read(descriptions: Boolean): MutableList<String?> {
        data class Frame(val node: AccessibilityNodeInfo, val count: Int, val depth: Int, var next: Int = 0)
        val start = SystemClock.elapsedRealtime(); var visits = 1; var characters = 0
        val stack = java.util.ArrayDeque<Frame>(); val texts = mutableListOf<String?>()
        fun check() { if (Thread.currentThread().isInterrupted) throw ReadAborted("INTERRUPTED"); if (SystemClock.elapsedRealtime() - start >= 2000) throw ReadAborted("TIME_LIMIT") }
        check(); val rootChildren = raw.childCount; branches = if (rootChildren > 0) 1 else 0; stack.push(Frame(raw, rootChildren, 0))
        try {
            while (stack.isNotEmpty()) {
                check(); val frame = stack.peek()
                if (frame.next >= frame.count) { stack.pop(); if (frame.node !== raw) arena.release(frame.node); continue }
                if (++visits > 2048) throw ReadAborted("NODE_LIMIT")
                if (frame.depth + 1 > 64) throw ReadAborted("DEPTH_LIMIT")
                val child = frame.node.getChild(frame.next++) ?: continue
                arena.own(child); check()
                if (stack.any { it.node == child }) throw ReadAborted("CYCLE")
                val count = child.childCount; check(); if (count > 0) branches++
                if (count == 0) {
                    val value = child.text?.takeIf { it.isNotEmpty() } ?: if (descriptions) child.contentDescription else null
                    check()
                    if (!value.isNullOrEmpty()) {
                        if (texts.size >= 512) throw ReadAborted("TEXT_LIMIT")
                        val text = value.toString()
                        if (text.length > 8192 || characters + text.length > 65536) throw ReadAborted("TEXT_CHAR_LIMIT")
                        characters += text.length; texts += text
                    }
                }
                stack.push(Frame(child, count, frame.depth + 1))
            }
            check()
            RuleBridge.context.get()?.let { it.tokens = texts.filterNotNull(); it.onRead(visits) }
            return texts
        } finally { while (stack.isNotEmpty()) { val frame = stack.pop(); if (frame.node !== raw) arena.release(frame.node) } }
    }
}
internal class ReadAborted(val reason: String) : RuntimeException(reason)
internal open class RuleBase {
    var a = 0
    var b = false
    var c = 0
    var f13681d: String? = null
    fun a(list: MutableList<String?>?, part: String?, exact: Boolean) = list!!.any { it != null && if (exact) it == part else it.contains(part!!) }
    fun b(map: HashMap<String?, Boolean>?, list: MutableList<String?>?) = list!!.any { map!!.containsKey(it) }
    fun c(list: MutableList<String?>?, part: String?, ordinal: Int, exact: Boolean): Int { var seen = 0; return list!!.indexOfFirst { if (it != null && if (exact) it == part else it.contains(part!!)) ++seen >= ordinal else false } }
    fun d(list: MutableList<String?>?, part: String?, exact: Boolean) = list!!.indexOfFirst { it != null && if (exact) it == part else it.contains(part!!) }
    fun e(list: MutableList<String?>?, part: String?, start: Int, exact: Boolean): Int { if (start >= list!!.size - 1) return -1; return (start until list.size).firstOrNull { val text = list[it]; text != null && if (exact) text == part else text.contains(part!!) } ?: -1 }
    fun f(list: MutableList<String?>?, part: String?, exact: Boolean) = list!!.indexOfLast { it != null && if (exact) it == part else it.contains(part!!) }
    fun g(node: CaptureNode?): MutableList<String?> = read(node, false)
    fun h(node: CaptureNode?): MutableList<String?> = read(node, false)
    fun i(node: CaptureNode?): MutableList<String?> = read(node, false)
    fun j(node: CaptureNode?): MutableList<String?> = read(node, false)
    fun k(node: CaptureNode?): MutableList<String?> = read(node, false)
    fun l(node: CaptureNode?): MutableList<String?> = read(node, false)
    fun m(node: CaptureNode?): MutableList<String?> = read(node, true)
    fun n(node: CaptureNode?): MutableList<String?> = read(node, true)
    fun o(map: HashMap<String?, Boolean>?, prefix: String?) = map!!.keys.any { it!!.startsWith(prefix!!) }
    private fun read(node: CaptureNode?, descriptions: Boolean): MutableList<String?> { c = 0; val result = node!!.read(descriptions); c = node.branches; return result }
    fun p(source: String?) { f13681d = source }
}

private class Bits(val value: Long)
internal class DexReference { var value: Any? = null }
private val zero = Bits(0); private val one = Bits(1); private val minusOne = Bits(-1)
internal fun dexN(value: Number): Any = when (val number = value.toLong()) { 0L -> zero; 1L -> one; -1L -> minusOne; else -> Bits(number) }
internal fun dexR(value: Any?): Any? = when (value) { is DexReference -> value.value; is Bits -> if (value.value == 0L) null else value; else -> value }
internal fun dexI(value: Any?): Int = (value as Bits).value.toInt()
internal fun dexL(value: Any?): Long = (value as Bits).value
internal fun dexD(value: Any?): Double = Double.fromBits(dexL(value))
internal fun dexF(value: Any?): Float = Float.fromBits(dexI(value))
internal fun dexZero(value: Any?): Boolean = value == null || value is Bits && value.value == 0L
internal fun dexZ(value: Any?): Boolean = !dexZero(value)
internal fun dexEqual(a: Any?, b: Any?): Boolean = if (a is Bits && b is Bits) a.value == b.value else dexR(a) === dexR(b)
internal fun dexCompareD(x: Any?, y: Any?, nan: Int): Int { val a = dexD(x); val b = dexD(y); return if (a.isNaN() || b.isNaN()) nan else if (a == b) 0 else if (a < b) -1 else 1 }
internal fun dexCompareF(x: Any?, y: Any?, nan: Int): Int { val a = dexF(x); val b = dexF(y); return if (a.isNaN() || b.isNaN()) nan else if (a == b) 0 else if (a < b) -1 else 1 }
internal fun dexLength(value: Any?): Int = when (value) { is Array<*> -> value.size; is IntArray -> value.size; is LongArray -> value.size; is ByteArray -> value.size; else -> error("Unsupported rule array") }

/**
 * Java's CharSequence overload is literal, including dollar signs and backslashes.
 * Use an explicit stdlib alias: an unqualified replace(String, String) here resolves
 * back to this same-package extension and recurses instead of calling the stdlib.
 */
internal fun String.replace(old: CharSequence, replacement: CharSequence): String =
    this.replaceLiteral(old.toString(), replacement.toString())

// Keep Java's default limit=0 behavior: discard trailing empty fields, retain leading ones.
internal fun javaSplit(input: String, pattern: String): Array<String?> = java.util.regex.Pattern.compile(pattern).split(input)
// Matcher preserves Java replacement groups ($1) and escapes; String.replaceFirst is literal in Kotlin.
internal fun replaceFirstRegex(input: String, pattern: String, replacement: String): String = java.util.regex.Pattern.compile(pattern).matcher(input).replaceFirst(replacement)
