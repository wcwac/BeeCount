package io.github.wcwac.beecount.domain

import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

/** Core invariants retained from reference MoneyTest and OriginalInstalmentScheduleTest. */
class MoneyTest {
    @Test fun decimalsNeverUseBinaryFloatingPoint() {
        assertEquals(29L, Money.parse("0.29").minor)
        assertEquals(123456L, Money.parse("1,234.56").minor)
        assertEquals("-0.01", Money(-1).decimal())
    }

    @Test fun currencyPrecisionIsEnforced() {
        assertEquals(12L, Money.parse("12", CurrencyCode("JPY")).minor)
        assertEquals(1234L, Money.parse("1.234", CurrencyCode("KWD")).minor)
        assertThrows(IllegalArgumentException::class.java) { Money.parse("1.1", CurrencyCode("JPY")) }
        assertThrows(IllegalArgumentException::class.java) { Money.parse("1.001") }
    }

    @Test fun rejectsAmbiguousOrUnboundedInput() {
        for (bad in listOf("NaN", "Infinity", "1e5", "1,23.00", "", "99999999999999999999999", "1000000000000.00")) {
            assertThrows(IllegalArgumentException::class.java) { Money.parse(bad) }
        }
        assertEquals(Money.MAX_INPUT_MINOR, Money.parse("999999999999.99").minor)
    }

    @Test fun instalmentAllocationConservesMoneyAndAnchorsMonthEnd() {
        val zone = ZoneId.of("UTC")
        val first = LocalDate.of(2025, 1, 31).atStartOfDay(zone).toInstant().toEpochMilli()
        val parts = InstalmentSchedule.create(1000, 101, 3, first, zone)
        assertEquals(listOf(334L, 333L, 333L), parts.map { it.principal })
        assertEquals(1000L, parts.sumOf { it.principal })
        assertEquals(101L, parts.sumOf { it.fee })
        assertEquals(listOf("2025-01-31", "2025-02-28", "2025-03-31"), parts.map {
            java.time.Instant.ofEpochMilli(it.dueAt).atZone(zone).toLocalDate().toString()
        })
        assertThrows(IllegalArgumentException::class.java) { InstalmentSchedule.create(1000, 0, 0, first, zone) }
    }
}
