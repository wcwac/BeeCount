package io.github.wcwac.beecount.nativeapp

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import io.github.wcwac.beecount.domain.CurrencyCode
import io.github.wcwac.beecount.domain.Money
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

open class NativeWidget : AppWidgetProvider() {
    protected open val repayment: Boolean = false
    override fun onUpdate(context: Context, manager: AppWidgetManager, appWidgetIds: IntArray) {
        val pending = goAsync()
        NativeApplication.container(context).scope.launch {
            try { update(context, appWidgetIds, repayment) } finally { pending.finish() }
        }
    }
    companion object {
        suspend fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            update(context, manager.getAppWidgetIds(ComponentName(context, NativeWidget::class.java)), false)
            update(context, manager.getAppWidgetIds(ComponentName(context, RepaymentWidget::class.java)), true)
        }
        private suspend fun update(context: Context, ids: IntArray, repayment: Boolean) = withContext(Dispatchers.IO) {
            if (ids.isEmpty()) return@withContext
            val container = NativeApplication.container(context)
            try { container.initialize() } catch (failure: Exception) {
                val fallback = RemoteViews(context.packageName, R.layout.native_widget).apply {
                    setTextViewText(R.id.widget_title, if (repayment) "近期还款" else "＋ 记一笔")
                    setTextViewText(R.id.widget_detail, "请打开应用完成数据检查")
                    setOnClickPendingIntent(R.id.widget_root, PendingIntent.getActivity(context, 3, Intent(context, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
                }
                AppWidgetManager.getInstance(context).updateAppWidget(ids, fallback)
                if (failure is kotlinx.coroutines.CancellationException) throw failure
                return@withContext
            }
            val preferences = container.settings.load()
            val text = if (repayment && !preferences.hideAmounts && !preferences.appLock) {
                val format = DateTimeFormatter.ofPattern("MM-dd").withZone(ZoneId.systemDefault())
                container.database.openHelper.readableDatabase.query("SELECT i.dueAt,i.principal+i.fee amount,e.currency,e.merchant FROM instalments i JOIN entries e ON e.id=i.entryId LEFT JOIN entries p ON p.id=i.paymentId WHERE e.deleted=0 AND (i.paymentId IS NULL OR p.deleted=1) ORDER BY i.dueAt LIMIT 3").use { cursor ->
                    buildList { while (cursor.moveToNext()) add("${format.format(Instant.ofEpochMilli(cursor.getLong(0)))}  ${Money(cursor.getLong(1), CurrencyCode(cursor.getString(2))).display()}") }.joinToString("\n").ifBlank { "暂无待还分期 · 点击查看账户账期" }
                }
            } else if (repayment) "打开应用查看还款记录" else "收支记录只保存在本机"
            val intent = Intent(context, MainActivity::class.java).putExtra("route", if (repayment) "assets" else "editor")
            val pending = PendingIntent.getActivity(context, if (repayment) 2 else 1, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            val views = RemoteViews(context.packageName, R.layout.native_widget).apply {
                setTextViewText(R.id.widget_title, if (repayment) "近期还款" else "＋ 记一笔")
                setTextViewText(R.id.widget_detail, text)
                setOnClickPendingIntent(R.id.widget_root, pending)
            }
            AppWidgetManager.getInstance(context).updateAppWidget(ids, views)
        }
    }
}

class RepaymentWidget : NativeWidget() { override val repayment = true }
