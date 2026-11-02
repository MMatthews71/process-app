package com.mmatthews.process.domain

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.random.Random

object DraftRules {
    const val RESUME_WINDOW_MS = 30 * 60 * 1000L

    fun shouldResume(draft: ProcessDraft?, now: Long): Boolean =
        draft != null && draft.step != Step.DONE && now - draft.updatedAt in 0..RESUME_WINDOW_MS
}

object TextRules {
    const val MIN_NEW_FACT_WORDS = 3

    fun wordCount(text: String): Int =
        text.trim().split(Regex("\\s+")).count { word -> word.any { it.isLetterOrDigit() } }

    /** Appends dictated text to whatever is already typed. */
    fun appendSpoken(existing: String, spoken: String): String {
        val add = spoken.trim()
        if (add.isEmpty()) return existing
        val base = existing.trimEnd()
        return if (base.isEmpty()) add.replaceFirstChar { it.uppercaseChar() } else "$base $add"
    }
}

object Prompts {
    fun withName(name: String, rest: String): String =
        if (name.isBlank()) rest.replaceFirstChar { it.uppercaseChar() } else "${name.trim()}, $rest"

    fun intensity(precise: String?, broad: String?): String {
        val word = precise ?: broad?.lowercase()
        return if (word == null) "How strong is it right now?" else "How strong is the $word right now?"
    }

    fun truer(name: String) = withName(name, "which part of this story might be true, and which part is overstated?")

    fun friend(name: String) = withName(name, "what would you tell a friend who did this?")
}

object PredictionRules {
    const val LOCK_MS = 7L * 24 * 60 * 60 * 1000
    const val FOLLOW_UP_AFTER_MS = 24L * 60 * 60 * 1000

    /**
     * When the "Test the prediction" option becomes available again, or null if it is
     * available now. [lastOtherPredictionAt] is the latest prediction from any *other*
     * event. If this event already holds its one prediction, the user is only editing it,
     * so it stays available.
     */
    fun nextAvailableAt(lastOtherPredictionAt: Long?, thisEventHasPrediction: Boolean, now: Long): Long? {
        if (thisEventHasPrediction) return null
        val until = (lastOtherPredictionAt ?: return null) + LOCK_MS
        return until.takeIf { it > now }
    }

    fun isFollowUpDue(createdAt: Long, outcome: PredictionOutcome?, now: Long): Boolean =
        outcome == null && now - createdAt >= FOLLOW_UP_AFTER_MS
}

object HelperPicker {
    /** One helper from the list, avoiding the one suggested last time when possible. */
    fun pick(helpers: List<String>, last: String?, random: Random = Random.Default): String? {
        val options = helpers.filter { it.isNotBlank() }
        if (options.isEmpty()) return null
        val pool = options.filter { it != last }.ifEmpty { options }
        return pool[random.nextInt(pool.size)]
    }
}

object DateText {
    private val short = DateTimeFormatter.ofPattern("EEE d MMM", Locale.getDefault())

    fun short(epochMillis: Long, zone: ZoneId = ZoneId.systemDefault()): String =
        short.format(Instant.ofEpochMilli(epochMillis).atZone(zone))

    fun elapsed(millis: Long): String {
        val total = (millis / 1000).coerceAtLeast(0)
        return "%d:%02d".format(total / 60, total % 60)
    }
}
