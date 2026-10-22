package com.tntlikely.beecount.capture

import org.junit.Assert.*
import org.junit.Test

class CapturePendingReadTest {
    private class Source(var released: Int = 0)
    private class Fixture {
        val tasks = mutableListOf<Runnable>()
        val owner = CapturePendingRead<Source>(
            post = { task, _ -> tasks.add(task) }, remove = { tasks.remove(it) },
            release = { it.released++ },
        )
    }

    @Test fun delayedReadReceivesItsOwnedSourceAndReleasesItAfterTheCallback() {
        val fixture = Fixture()
        val source = Source()
        var observed: Source? = null
        fixture.owner.replace(source, 150) { observed = it; assertEquals(0, source.released) }
        assertNull(observed)
        val task = fixture.tasks.single()
        task.run()
        assertSame(source, observed)
        assertEquals(1, source.released)
        task.run()
        fixture.owner.cancel()
        assertEquals(1, source.released)
    }

    @Test fun replacingAReadReleasesTheOldSourceWithoutLettingItsStaleTaskConsumeTheNewOne() {
        val fixture = Fixture()
        val old = Source()
        val next = Source()
        var observed: Source? = null
        fixture.owner.replace(old, 150) { fail("Replaced callback must not run") }
        val stale = fixture.tasks.single()
        fixture.owner.replace(next, 150) { observed = it }
        assertEquals(1, old.released)
        stale.run()
        assertNull(observed)
        assertEquals(0, next.released)
        fixture.tasks.single().run()
        assertSame(next, observed)
        assertEquals(1, old.released)
        assertEquals(1, next.released)
    }

    @Test fun cancellingForSettingsOrDisconnectReleasesTheSourceAndInvalidatesItsCallback() {
        val fixture = Fixture()
        val source = Source()
        fixture.owner.replace(source, 150) { fail("Cancelled callback must not run") }
        val stale = fixture.tasks.single()
        fixture.owner.cancel()
        fixture.owner.cancel()
        stale.run()
        assertTrue(fixture.tasks.isEmpty())
        assertEquals(1, source.released)
    }

    @Test fun aThrowingReaderStillReleasesItsSource() {
        val fixture = Fixture()
        val source = Source()
        fixture.owner.replace(source, 150) { throw IllegalStateException("fixture") }
        try { fixture.tasks.single().run(); fail("Reader failure must propagate") }
        catch (_: IllegalStateException) { /* Expected. */ }
        fixture.owner.cancel()
        assertEquals(1, source.released)
    }

    @Test fun failureToScheduleReleasesTheSource() {
        val source = Source()
        val owner = CapturePendingRead<Source>(
            post = { _, _ -> throw IllegalStateException("fixture") }, remove = {}, release = { it.released++ })
        try { owner.replace(source, 150) {}; fail("Scheduling failure must propagate") }
        catch (_: IllegalStateException) { /* Expected. */ }
        owner.cancel()
        assertEquals(1, source.released)
    }
}
