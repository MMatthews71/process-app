package com.mmatthews.process.domain

enum class EntryType { PROCESS, NOTICE, GOOD }

enum class ReframeType { TRUER, FRIEND, NONE }

enum class ActionType { LET_GO, REPAIR, PREDICTION, IF_THEN, THERAPY, NONE }

enum class PredictionOutcome(val label: String) {
    BETTER("Better than predicted"),
    AS_PREDICTED("About as predicted"),
    WORSE("Worse"),
    DIDNT_ASK("Didn't ask"),
}

enum class ScoreType { SOCIAL_ANXIETY, PHQ9 }

/** Everything the UI needs from DataStore, as plain values. */
data class AppSettings(
    val onboarded: Boolean = false,
    val breathCycles: Int = Defaults.BREATH_CYCLES,
    val feelings: Map<String, List<String>> = Defaults.feelings,
    val customChips: List<String> = emptyList(),
    val helpers: List<String> = Defaults.helpers,
    val lastReframeTab: ReframeType = ReframeType.TRUER,
    val worrySlotEnabled: Boolean = false,
    val lastHelper: String? = null,
) {
    val storyChips: List<String> get() = Defaults.storyChips + customChips
}
