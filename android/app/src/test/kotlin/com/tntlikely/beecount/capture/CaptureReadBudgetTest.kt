package com.tntlikely.beecount.capture

import org.junit.Assert.*
import org.junit.Test

class CaptureReadBudgetTest {
    @Test fun sourceGetsAReservedSliceAfterTheWindowSliceExpires() {
        var now = 0L
        val budget = CaptureReadBudget { now }
        now = 24
        assertTrue(budget.timeExhausted())
        budget.beginSourceAttempt()
        assertFalse(budget.timeExhausted())
        now = 35
        assertFalse(budget.timeExhausted())
        now = 36
        assertTrue(budget.timeExhausted())
    }

    @Test fun anEarlySourceAttemptStillHasOnlyTwelveMilliseconds() {
        var now = 0L
        val budget = CaptureReadBudget { now }
        now = 5
        budget.beginSourceAttempt()
        now = 16
        assertFalse(budget.timeExhausted())
        now = 17
        assertTrue(budget.timeExhausted())
    }

    @Test fun beginningASourceAttemptCannotResetTheTotalDeadlineOrCounters() {
        var now = 0L
        val budget = CaptureReadBudget { now }
        budget.visited = 400
        budget.characters = 6000
        now = 40
        budget.beginSourceAttempt()
        assertTrue(budget.timeExhausted())
        assertEquals(400, budget.visited)
        assertEquals(6000, budget.characters)
    }
}
