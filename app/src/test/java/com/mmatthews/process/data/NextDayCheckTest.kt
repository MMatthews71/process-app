package com.mmatthews.process.data

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NextDayCheckTest {
    private val hour = 60 * 60 * 1000L

    private fun entry(id: Long, createdAt: Long, outcome: Outcome? = null, story: String = "They don't like me") =
        Entry(id = id, createdAt = createdAt, happened = "I waved and they didn't wave back", story = story, outcome = outcome)

    @Test
    fun dueOnlyAfter20Hours() {
        val e = entry(1, createdAt = 0)
        assertNull(NextDayCheck.pick(listOf(e), now = 20 * hour - 1))
        assertEquals(e, NextDayCheck.pick(listOf(e), now = 20 * hour))
        assertEquals(e, NextDayCheck.pick(listOf(e), now = 72 * hour))
    }

    @Test
    fun oldestUnansweredFirst() {
        val older = entry(1, createdAt = 0)
        val newer = entry(2, createdAt = 2 * hour)
        val answered = entry(3, createdAt = -5 * hour, outcome = Outcome.NO)
        assertEquals(older, NextDayCheck.pick(listOf(newer, answered, older), now = 30 * hour))
    }

    @Test
    fun entriesYoungerThan20HoursAreSkipped() {
        val due = entry(1, createdAt = 0)
        val fresh = entry(2, createdAt = 25 * hour)
        assertEquals(due, NextDayCheck.pick(listOf(fresh, due), now = 30 * hour))
    }

    @Test
    fun atMostOneCheckPerOpen() = runBlocking {
        val dao = FakeEntryDao()
        val store = EntryStore(dao)
        store.save("A", "I'm a loser", "", now = 0)
        store.save("B", "I hurt someone", "", now = hour)

        val open = AppOpen(store)
        val first = open.takeCheck(now = 48 * hour)
        assertEquals("A", first?.happened)
        assertNull(open.takeCheck(now = 48 * hour))

        // The next open gets the next oldest, once the first is answered.
        store.answer(first!!.id, Outcome.YES)
        assertEquals("B", AppOpen(store).takeCheck(now = 48 * hour)?.happened)
    }

    @Test
    fun answeredEntryIsNotCheckedAgain() = runBlocking {
        val store = EntryStore(FakeEntryDao())
        val e = store.save("A", "They think I'm weird", "", now = 0)
        store.answer(e.id, Outcome.UNKNOWN)
        assertNull(AppOpen(store).takeCheck(now = 48 * hour))
    }

    @Test
    fun headlineIsStoryOrWhatHappenedForOther() {
        assertEquals("They don't like me", NextDayCheck.headline(entry(1, 0)))
        assertEquals("I waved and they didn't wave back", NextDayCheck.headline(entry(1, 0, story = Stories.OTHER)))
    }
}
