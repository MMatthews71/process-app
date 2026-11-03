package com.mmatthews.process.ui

enum class BreathPhase(val word: String, val millis: Long, val scale: Float) {
    IN("In", 2_500, 0.85f),
    SIP("Sip", 1_000, 1.0f),
    OUT("Out", 6_000, REST_SCALE),
}

const val REST_SCALE = 0.45f
const val BREATHS = 5
