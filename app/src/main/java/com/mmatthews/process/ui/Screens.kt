package com.mmatthews.process.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mmatthews.process.data.Entry
import com.mmatthews.process.data.Outcome
import kotlinx.coroutines.delay

/** Screen 1. Five cyclic sighs, then [onFinished]. Long-press the circle to skip. */
@Composable
fun BreatheScreen(onPhase: () -> Unit, onFinished: () -> Unit, onRules: () -> Unit) {
    val p = LocalPalette.current
    var phase by remember { mutableStateOf<BreathPhase?>(null) }
    var finished by remember { mutableStateOf(false) }
    val pulse by rememberUpdatedState(onPhase)
    val done by rememberUpdatedState(onFinished)
    val finish = {
        if (!finished) {
            finished = true
            done()
        }
    }

    LaunchedEffect(Unit) {
        repeat(BREATHS) {
            for (next in BreathPhase.entries) {
                phase = next
                pulse()
                delay(next.millis)
            }
        }
        finish()
    }

    val current = phase
    val scale by animateFloatAsState(
        targetValue = current?.scale ?: REST_SCALE,
        animationSpec = tween(durationMillis = current?.millis?.toInt() ?: 0, easing = FastOutSlowInEasing),
        label = "breath",
    )

    Box(Modifier.fillMaxSize()) {
        Box(
            Modifier
                .align(Alignment.Center)
                .size(280.dp)
                .pointerInput(Unit) { detectTapGestures(onLongPress = { finish() }) }
                .semantics { onLongClick(label = "Skip") { finish(); true } },
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .clip(CircleShape)
                    .background(p.accent),
            )
            Text(current?.word.orEmpty(), style = TextSizes.Large, color = p.background)
        }
        Text(
            "Rules",
            style = TextSizes.Normal,
            color = p.text,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    role = Role.Button,
                    onClick = onRules,
                )
                .padding(20.dp),
        )
    }
}

/** Screen 2. */
@Composable
fun SplitScreen(
    happened: String,
    onHappened: (String) -> Unit,
    stories: List<String>,
    story: String?,
    onStory: (String) -> Unit,
    onNext: () -> Unit,
) {
    Centered {
        PlainField(happened, onHappened, "What happened? (camera view)")
        Pills(stories, story, onStory)
        AccentButton("Next", onNext, enabled = happened.isNotBlank() && story != null)
    }
}

/** Screen 3. */
@Composable
fun NextTimeScreen(nextTime: String, onNextTime: (String) -> Unit, onDone: () -> Unit) {
    Centered {
        PlainField(nextTime, onNextTime, "Next time I'll… (optional)")
        AccentButton("Done", onDone)
    }
}

/** Shown before screen 1 when an entry from 20+ hours ago has no outcome yet. */
@Composable
fun CheckScreen(headline: String, onAnswer: (Outcome) -> Unit) {
    val p = LocalPalette.current
    Centered(spacing = 16) {
        Text(headline, style = TextSizes.Large, color = p.text, textAlign = TextAlign.Center)
        Text(
            "Was it true?",
            style = TextSizes.Normal,
            color = p.text,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp),
        )
        AccentButton("Yes", { onAnswer(Outcome.YES) })
        AccentButton("No", { onAnswer(Outcome.NO) })
        AccentButton("Didn't find out", { onAnswer(Outcome.UNKNOWN) })
    }
}

/** Saved rules, newest first. Long-press one to delete it. */
@Composable
fun RulesScreen(rules: List<Entry>, onDelete: (Long) -> Unit) {
    val p = LocalPalette.current
    Column(Modifier.fillMaxSize()) {
        Centered(Modifier.weight(1f).fillMaxWidth(), spacing = 8) {
            rules.forEach { entry ->
                Text(
                    "Next time I'll ${entry.rule}",
                    style = TextSizes.Normal,
                    color = p.text,
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerInput(entry.id) { detectTapGestures(onLongPress = { onDelete(entry.id) }) }
                        .semantics { onLongClick(label = "Delete") { onDelete(entry.id); true } }
                        .padding(vertical = 12.dp),
                )
            }
        }
        Text(
            "Lifeline 13 11 14 · Canada 988",
            style = TextSizes.Normal,
            color = p.text,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(16.dp),
        )
    }
}
