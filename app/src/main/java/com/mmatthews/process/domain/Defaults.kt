package com.mmatthews.process.domain

object Defaults {
    const val BREATH_CYCLES = 5
    const val INTENSITY = 6
    const val PREDICTION_PERCENT = 50

    val feelings: Map<String, List<String>> = linkedMapOf(
        "Anxious" to listOf("on edge", "dreading", "panicky", "exposed"),
        "Ashamed" to listOf("humiliated", "foolish", "embarrassed", "inadequate"),
        "Sad" to listOf("disappointed", "hurt", "hopeless", "empty"),
        "Angry" to listOf("frustrated", "resentful", "irritated"),
        "Lonely" to listOf("left out", "rejected", "disconnected"),
        "Guilty" to listOf("regretful", "remorseful"),
    )

    val storyChips: List<String> = listOf(
        "They think I'm weird",
        "I'm not good enough",
        "They don't like me",
        "I'll be rejected",
        "I hurt someone",
        "I'm a loser",
    )

    val helpers: List<String> = listOf(
        "Call family",
        "Step into sunlight",
        "Stretch 2 min",
    )

    val crisisLines: List<CrisisLine> = listOf(
        CrisisLine("Canada", "988"),
    )
}
