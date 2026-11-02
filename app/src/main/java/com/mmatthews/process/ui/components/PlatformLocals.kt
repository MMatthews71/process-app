package com.mmatthews.process.ui.components

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import com.mmatthews.process.domain.CrisisLine
import com.mmatthews.process.domain.Defaults

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

fun interface Dialer {
    fun dial(number: String)
}

val LocalVoiceInput = staticCompositionLocalOf<VoiceInput> { NoVoiceInput }
val LocalHaptics = staticCompositionLocalOf { Haptics { _, _ -> } }
val LocalDialer = staticCompositionLocalOf { Dialer { } }
val LocalCrisisLines = compositionLocalOf { Defaults.crisisLines }
