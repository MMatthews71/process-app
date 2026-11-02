package com.mmatthews.process.domain

/** Cyclic sighing: two inhales through the nose, then a long exhale through the mouth. */
enum class BreathPhase(val label: String, val millis: Long, val scale: Float, val pulseMillis: Long, val strongPulse: Boolean) {
    INHALE("In", 2_000, 0.8f, 60, true),
    SIP("Sip", 1_000, 1.0f, 35, true),
    EXHALE("Out", 6_000, 0.4f, 220, false),
}

object BreathPlan {
    const val INSTRUCTION = "In through the nose, a second short sip in, long slow breath out through the mouth."
    val cycleMillis: Long = BreathPhase.entries.sumOf { it.millis }
    const val MAX_TOTAL_MS = 5 * 60 * 1000L
    const val SKIP_AFTER_MS = 20_000L
    const val ALLOW_MS = 15_000L
    const val GROUND_MS = 10_000L

    /** Whether another full cycle fits in the 5-minute cap. */
    fun canRunAnotherCycle(breathedMillis: Long): Boolean = breathedMillis + cycleMillis <= MAX_TOTAL_MS
}
