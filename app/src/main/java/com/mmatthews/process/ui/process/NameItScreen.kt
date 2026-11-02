package com.mmatthews.process.ui.process

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.mmatthews.process.domain.ProcessDraft
import com.mmatthews.process.domain.Prompts
import com.mmatthews.process.ui.components.ChoiceButton
import com.mmatthews.process.ui.components.ChoiceRow
import com.mmatthews.process.ui.components.FlowScaffold
import com.mmatthews.process.ui.components.IntensitySlider
import com.mmatthews.process.ui.components.PrimaryButton
import com.mmatthews.process.ui.components.Prompt

/** Screen 1 — Name it. Broad feeling, then a precise word (optional second), then strength. */
@Composable
fun NameItScreen(d: ProcessDraft, feelings: Map<String, List<String>>, a: ProcessActions) {
    val broad = d.feelingBroad
    val precise = d.feelingPrecise
    FlowScaffold(
        onBack = a::back,
        onSkip = a::skip,
        bottom = { if (precise != null) PrimaryButton("Next", a::nameItNext) },
    ) {
        Prompt(
            when {
                broad == null -> "What are you feeling?"
                precise == null -> "Which word fits best?"
                else -> Prompts.intensity(precise, broad)
            },
        )
        ChoiceRow {
            feelings.keys.forEach { name ->
                ChoiceButton(name, selected = name == broad, onClick = { a.selectBroad(name) })
            }
        }
        if (broad != null) {
            ChoiceRow {
                feelings[broad].orEmpty().forEach { word ->
                    ChoiceButton(
                        word,
                        selected = word == precise || word == d.feelingPrecise2,
                        onClick = { a.togglePrecise(word) },
                    )
                }
            }
        }
        if (precise != null) {
            if (d.feelingPrecise2 == null) {
                Text(
                    "Tap a second word if it fits too.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IntensitySlider(d.intensitySlider, a::setIntensity)
        }
    }
}
