package com.mmatthews.process.ui.process

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mmatthews.process.domain.BreathPhase
import com.mmatthews.process.domain.BreathPlan
import com.mmatthews.process.domain.DateText
import com.mmatthews.process.ui.components.FlowScaffold
import com.mmatthews.process.ui.components.LocalHaptics
import com.mmatthews.process.ui.components.PrimaryButton
import com.mmatthews.process.ui.components.Prompt
import com.mmatthews.process.ui.components.SecondaryButton
import kotlinx.coroutines.delay

enum class BodyPhase { BREATHE, CHOICE, EXTENDED, ALLOW, GROUND }

private const val TICK_MS = 100L

/**
 * Screen 2 — Body. [cycles] cyclic sighs, then "Keep going" (up to 5 min total) or "Next",
 * then 15 s allow, then 10 s grounding. Skip appears after 20 s.
 */
@Composable
fun BodyScreen(cycles: Int, onBack: () -> Unit, onDone: (breathSeconds: Int) -> Unit) {
    var phase by rememberSaveable { mutableStateOf(BodyPhase.BREATHE) }
    var breathedMs by rememberSaveable { mutableLongStateOf(0L) }
    var breath by remember { mutableStateOf(BreathPhase.INHALE) }
    var cycle by remember { mutableStateOf(1) }
    var showSkip by rememberSaveable { mutableStateOf(false) }
    val haptics = LocalHaptics.current
    val finish by rememberUpdatedState { onDone((breathedMs / 1000).toInt()) }

    LaunchedEffect(Unit) {
        delay(BreathPlan.SKIP_AFTER_MS)
        showSkip = true
    }

    LaunchedEffect(phase) {
        suspend fun runCycle() {
            for (p in BreathPhase.entries) {
                breath = p
                haptics.pulse(p.pulseMillis, p.strongPulse)
                var left = p.millis
                while (left > 0) {
                    val step = minOf(TICK_MS, left)
                    delay(step)
                    left -= step
                    breathedMs += step
                }
            }
        }
        when (phase) {
            BodyPhase.BREATHE -> {
                for (i in 1..cycles.coerceAtLeast(1)) {
                    cycle = i
                    runCycle()
                }
                phase = BodyPhase.CHOICE
            }
            BodyPhase.EXTENDED -> {
                while (BreathPlan.canRunAnotherCycle(breathedMs)) runCycle()
                phase = BodyPhase.ALLOW
            }
            BodyPhase.ALLOW -> {
                delay(BreathPlan.ALLOW_MS)
                phase = BodyPhase.GROUND
            }
            BodyPhase.GROUND -> {
                delay(BreathPlan.GROUND_MS)
                finish()
            }
            BodyPhase.CHOICE -> Unit
        }
    }

    val breathing = phase == BodyPhase.BREATHE || phase == BodyPhase.EXTENDED
    FlowScaffold(
        onBack = onBack,
        onSkip = if (showSkip) finish else null,
        bottom = {
            when (phase) {
                BodyPhase.CHOICE -> {
                    SecondaryButton("Keep going", { phase = BodyPhase.EXTENDED })
                    PrimaryButton("Next", { phase = BodyPhase.ALLOW })
                }
                BodyPhase.EXTENDED -> PrimaryButton("Next", { phase = BodyPhase.ALLOW })
                else -> Unit
            }
        },
    ) {
        when (phase) {
            BodyPhase.BREATHE, BodyPhase.EXTENDED, BodyPhase.CHOICE -> {
                Prompt(BreathPlan.INSTRUCTION)
                BreathCircle(
                    scale = if (breathing) breath.scale else 0.6f,
                    durationMs = if (breathing) breath.millis.toInt() else 600,
                    label = if (breathing) breath.label else "",
                )
                Text(
                    when (phase) {
                        BodyPhase.BREATHE -> "$cycle of $cycles"
                        else -> DateText.elapsed(breathedMs)
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            BodyPhase.ALLOW -> {
                Spacer(Modifier.height(48.dp))
                Prompt("Let it be there. You don't need to fix it.")
            }
            BodyPhase.GROUND -> {
                Spacer(Modifier.height(48.dp))
                Prompt("Name 3 things you can hear or see.")
            }
        }
    }
}

@Composable
private fun BreathCircle(scale: Float, durationMs: Int, label: String) {
    val animated by animateFloatAsState(
        targetValue = scale,
        animationSpec = tween(durationMillis = durationMs, easing = FastOutSlowInEasing),
        label = "breath",
    )
    Box(Modifier.fillMaxWidth().height(280.dp), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .size(260.dp)
                .graphicsLayer {
                    scaleX = animated
                    scaleY = animated
                }
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
        )
        Text(label, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}
