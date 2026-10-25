package io.github.wcwac.beecount.domain

import java.time.Instant
import java.time.ZoneId

data class InstalmentPart(val sequence: Int, val dueAt: Long, val principal: Long, val fee: Long)

/** Splits integer minor units without rounding loss; every date uses the original day. */
object InstalmentSchedule {
    fun create(principal: Long, fee: Long, periods: Int, firstDueAt: Long, zone: ZoneId = ZoneId.systemDefault()): List<InstalmentPart> {
        require(periods in 1..120 && principal in periods.toLong()..Money.MAX_INPUT_MINOR && fee in 0..Money.MAX_INPUT_MINOR && firstDueAt > 0) { "期数、金额或首次还款日期无效" }
        val first = Instant.ofEpochMilli(firstDueAt).atZone(zone)
        return List(periods) { index ->
            InstalmentPart(index + 1, first.plusMonths(index.toLong()).toInstant().toEpochMilli(),
                principal / periods + if (index < principal % periods) 1 else 0,
                fee / periods + if (index < fee % periods) 1 else 0)
        }
    }
}
