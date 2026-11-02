package com.mmatthews.process.ui.process

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.mmatthews.process.domain.ActStage
import com.mmatthews.process.domain.ActionType
import com.mmatthews.process.domain.DateText
import com.mmatthews.process.domain.Flow
import com.mmatthews.process.ui.components.ChoiceButton
import com.mmatthews.process.ui.components.FieldLabel
import com.mmatthews.process.ui.components.FlowScaffold
import com.mmatthews.process.ui.components.PrimaryButton
import com.mmatthews.process.ui.components.Prompt
import com.mmatthews.process.ui.components.SecondaryButton
import com.mmatthews.process.ui.components.VoiceTextField
import kotlin.math.roundToInt

/** Screen 4 — Act. Pick one action, fill in its one step, save. */
@Composable
fun ActScreen(ui: ProcessUi, a: ProcessActions) {
    val d = ui.draft
    val save: @Composable () -> Unit = { PrimaryButton("Save", a::saveAction, enabled = Flow.canSaveAction(d)) }
    when (d.actStage) {
        ActStage.CHOOSE -> FlowScaffold(onBack = a::back, onSkip = a::skip) {
            Prompt("Pick one.")
            ChoiceButton("Let it go", false, { a.chooseAction(ActionType.LET_GO) }, fullWidth = true)
            ChoiceButton("Repair", false, { a.chooseAction(ActionType.REPAIR) }, fullWidth = true)
            val locked = ui.predictionLockedUntil
            if (locked == null) {
                ChoiceButton("Test the prediction", false, { a.chooseAction(ActionType.PREDICTION) }, fullWidth = true)
            } else {
                OutlinedButton(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Test the prediction", style = MaterialTheme.typography.titleMedium)
                        Text("next available ${DateText.short(locked)}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            ChoiceButton("If-then plan", false, { a.chooseAction(ActionType.IF_THEN) }, fullWidth = true)
            ChoiceButton("Bring to therapy", false, { a.chooseAction(ActionType.THERAPY) }, fullWidth = true)
        }

        ActStage.LET_GO -> FlowScaffold(onBack = a::back, onSkip = null, bottom = { save() }) {
            Prompt("If it comes back, notice it and leave it.")
            if (ui.settings.worrySlotEnabled) {
                Text("It can wait for Sunday's slot.", style = MaterialTheme.typography.titleMedium)
            }
        }

        ActStage.REPAIR_ASK -> FlowScaffold(
            onBack = a::back,
            onSkip = null,
            bottom = {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SecondaryButton("No", { a.repairAnswer(false) }, Modifier.weight(1f))
                    PrimaryButton("Yes", { a.repairAnswer(true) }, Modifier.weight(1f))
                }
            },
        ) {
            Prompt("Would a neutral observer say something concrete went wrong for the other person?")
        }

        ActStage.REPAIR_YES -> FlowScaffold(onBack = a::back, onSkip = null, bottom = { save() }) {
            Prompt("What will I do, and when?")
            VoiceTextField(d.repairNote, a::setRepairNote, testTag = "repairNote")
        }

        ActStage.REPAIR_NO -> FlowScaffold(onBack = a::back, onSkip = null, bottom = { save() }) {
            Prompt("Then it's probably not something to fix.")
        }

        ActStage.PREDICTION -> FlowScaffold(onBack = a::back, onSkip = null, bottom = { save() }) {
            VoiceTextField(
                d.predictionText,
                a::setPredictionText,
                testTag = "predictionText",
                label = "The prediction",
                minLines = 1,
            )
            FieldLabel("How likely? ${d.predictionPercent}%")
            Slider(
                value = d.predictionPercent.toFloat(),
                onValueChange = { a.setPredictionPercent(it.roundToInt()) },
                valueRange = 0f..100f,
                steps = 19,
                modifier = Modifier.fillMaxWidth().testTag("predictionSlider"),
            )
            Prompt("Ask them once, neutrally, then log what happened.")
        }

        ActStage.IF_THEN -> FlowScaffold(onBack = a::back, onSkip = null, bottom = { save() }) {
            VoiceTextField(d.ifCue, a::setIfCue, testTag = "ifCue", label = "If ___", minLines = 1)
            VoiceTextField(d.ifAction, a::setIfAction, testTag = "ifAction", label = "then I will ___", minLines = 1)
        }
    }
}
