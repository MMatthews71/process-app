package com.mmatthews.process.data

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EntryStoreTest {

    @Test
    fun savingWithoutARuleIsLetItGo() = runBlocking {
        val dao = FakeEntryDao()
        val store = EntryStore(dao)
        val saved = store.save("  They left early  ", "They don't like me", "   ", now = 100)

        val row = dao.entries.value.single()
        assertEquals(saved, row)
        assertEquals("They left early", row.happened)
        assertEquals("They don't like me", row.story)
        assertEquals(100L, row.createdAt)
        assertNull(row.rule)
        assertNull(row.outcome)
        assertTrue(store.rules().first().isEmpty())
    }

    @Test
    fun savingWithARuleAddsItToRules() = runBlocking {
        val dao = FakeEntryDao()
        val store = EntryStore(dao)
        store.save("Said something odd", "They think I'm weird", " ask one question ", now = 100)

        assertEquals("ask one question", dao.entries.value.single().rule)
        assertEquals(listOf("ask one question"), store.rules().first().map { it.rule })
    }

    @Test
    fun typedPrefixIsNotDoubled() {
        assertEquals("ask one question", EntryStore.cleanRule("Next time I'll ask one question"))
        assertEquals("breathe first", EntryStore.cleanRule("next time i will breathe first"))
        assertNull(EntryStore.cleanRule("Next time I'll"))
        assertNull(EntryStore.cleanRule(""))
    }

    @Test
    fun rulesAreNewestFirst() = runBlocking {
        val store = EntryStore(FakeEntryDao())
        store.save("a", "I'm a loser", "first", now = 1)
        store.save("b", "I'm a loser", "", now = 2)
        store.save("c", "I'm a loser", "second", now = 3)
        assertEquals(listOf("second", "first"), store.rules().first().map { it.rule })
    }

    @Test
    fun deletingARuleRemovesItButKeepsTheEntry() = runBlocking {
        val dao = FakeEntryDao()
        val store = EntryStore(dao)
        val keep = store.save("a", "I hurt someone", "apologise", now = 1)
        val drop = store.save("b", "I hurt someone", "text them", now = 2)

        store.deleteRule(drop.id)

        assertEquals(listOf("apologise"), store.rules().first().map { it.rule })
        assertEquals(2, dao.entries.value.size)
        assertEquals(keep.id, store.rules().first().single().id)
        // The entry still gets its next-day check.
        assertEquals(keep.id, store.dueCheck(now = 48L * 60 * 60 * 1000)?.id)
    }
}
