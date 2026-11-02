package com.mmatthews.process.ui.components

import androidx.compose.runtime.staticCompositionLocalOf

/** Speech-to-text. [activeKey] is the field currently listening, if any (snapshot state). */
interface VoiceInput {
    val activeKey: String?
    fun start(key: String, onText: (String) -> Unit)
    fun stop()
}

object NoVoiceInput : VoiceInput {
    override val activeKey: String? = null
    override fun start(key: String, onText: (String) -> Unit) = Unit
    override fun stop() = Unit
}

fun interface Haptics {
    fun pulse(millis: Long, strong: Boolean)
}

val LocalVoiceInput = staticCompositionLocalOf<VoiceInput> { NoVoiceInput }
val LocalHaptics = staticCompositionLocalOf { Haptics { _, _ -> } }
