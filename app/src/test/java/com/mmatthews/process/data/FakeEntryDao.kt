package com.mmatthews.process.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** In-memory EntryDao that behaves like the Room queries. */
class FakeEntryDao : EntryDao {
    val entries = MutableStateFlow<List<Entry>>(emptyList())
    private var nextId = 1L

    override suspend fun insert(entry: Entry): Long {
        val id = nextId++
        entries.value = entries.value + entry.copy(id = id)
        return id
    }

    override suspend fun unanswered(): List<Entry> =
        entries.value.filter { it.outcome == null }.sortedBy { it.createdAt }

    override suspend fun setOutcome(id: Long, outcome: String) {
        entries.value = entries.value.map { if (it.id == id) it.copy(outcome = Outcome.valueOf(outcome)) else it }
    }

    override fun rules(): Flow<List<Entry>> =
        entries.map { list -> list.filter { it.rule != null }.sortedByDescending { it.createdAt } }

    override suspend fun deleteRule(id: Long) {
        entries.value = entries.value.map { if (it.id == id) it.copy(rule = null) else it }
    }
}
