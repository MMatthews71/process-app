package com.mmatthews.process.data

import kotlinx.coroutines.flow.Flow

class EntryStore(private val dao: EntryDao) {

    /** Saves an entry. A blank "next time" counts as "let it go" (rule = null). */
    suspend fun save(happened: String, story: String, nextTime: String, now: Long): Entry {
        val entry = Entry(
            createdAt = now,
            happened = happened.trim(),
            story = story,
            rule = cleanRule(nextTime),
        )
        return entry.copy(id = dao.insert(entry))
    }

    suspend fun dueCheck(now: Long): Entry? = NextDayCheck.pick(dao.unanswered(), now)

    suspend fun answer(id: Long, outcome: Outcome) = dao.setOutcome(id, outcome.name)

    fun rules(): Flow<List<Entry>> = dao.rules()

    suspend fun deleteRule(id: Long) = dao.deleteRule(id)

    companion object {
        private val prefix = Regex("^next time i(?:'ll|’ll| will)\\s*", RegexOption.IGNORE_CASE)

        /** Trims, and drops a typed "Next time I'll" so the Rules list doesn't repeat it. */
        fun cleanRule(text: String): String? =
            text.trim().replace(prefix, "").trim().takeIf { it.isNotEmpty() }
    }
}

object NextDayCheck {
    const val DUE_AFTER_MS = 20L * 60 * 60 * 1000

    /** The oldest entry without an outcome that is at least 20 hours old. */
    fun pick(entries: List<Entry>, now: Long): Entry? =
        entries
            .filter { it.outcome == null && now - it.createdAt >= DUE_AFTER_MS }
            .minByOrNull { it.createdAt }

    /** Large text on the check screen: the story, or what happened if the story was "Other". */
    fun headline(entry: Entry): String =
        if (entry.story == Stories.OTHER) entry.happened else entry.story
}

/** One app open: hands out at most one next-day check. */
class AppOpen(private val store: EntryStore) {
    private var used = false

    suspend fun takeCheck(now: Long): Entry? {
        if (used) return null
        used = true
        return store.dueCheck(now)
    }
}
