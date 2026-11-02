package com.mmatthews.process.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mmatthews.process.domain.PredictionOutcome
import com.mmatthews.process.ui.components.AppIcons
import com.mmatthews.process.ui.components.ChoiceButton
import com.mmatthews.process.ui.components.CrisisFooter
import com.mmatthews.process.ui.components.PrimaryButton
import com.mmatthews.process.ui.components.VoiceTextField

data class FollowUpUi(val entryId: Long, val prediction: String, val percent: Int?)

/**
 * Home. Lessons, Therapy and Settings are built in later steps, so their icons are
 * shown disabled for now.
 */
@Composable
fun HomeScreen(
    followUp: FollowUpUi?,
    onProcess: () -> Unit,
    onGood: () -> Unit,
    onAnswerFollowUp: (FollowUpUi, PredictionOutcome, String) -> Unit,
    lessonsEnabled: Boolean = false,
    therapyEnabled: Boolean = false,
    settingsEnabled: Boolean = false,
) {
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Row(Modifier.fillMaxWidth().height(56.dp)) {
            Spacer(Modifier.weight(1f))
            IconButton(onClick = {}, enabled = lessonsEnabled, modifier = Modifier.size(56.dp)) {
                Icon(AppIcons.Lessons, contentDescription = "Lessons")
            }
            IconButton(onClick = {}, enabled = therapyEnabled, modifier = Modifier.size(56.dp)) {
                Icon(AppIcons.Therapy, contentDescription = "Therapy list")
            }
            IconButton(onClick = {}, enabled = settingsEnabled, modifier = Modifier.size(56.dp)) {
                Icon(Icons.Filled.Settings, contentDescription = "Settings")
            }
        }
        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (followUp != null) FollowUpCard(followUp, onAnswerFollowUp)
            Spacer(Modifier.weight(1f))
            Button(onClick = onProcess, modifier = Modifier.fillMaxWidth().height(160.dp)) {
                Text("Process it", style = MaterialTheme.typography.headlineMedium)
            }
            OutlinedButton(onClick = onGood, modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp)) {
                Text("Good thing", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.weight(1f))
        }
        CrisisFooter()
    }
}

@Composable
private fun FollowUpCard(f: FollowUpUi, onAnswer: (FollowUpUi, PredictionOutcome, String) -> Unit) {
    var chosen by rememberSaveable(f.entryId) { mutableStateOf<PredictionOutcome?>(null) }
    var note by rememberSaveable(f.entryId) { mutableStateOf("") }
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("What happened?", style = MaterialTheme.typography.titleLarge)
            Text(
                "You predicted: ${f.prediction}" + (f.percent?.let { " · $it%" } ?: ""),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            PredictionOutcome.entries.forEach { o ->
                ChoiceButton(o.label, selected = o == chosen, onClick = { chosen = o }, fullWidth = true)
            }
            val picked = chosen
            if (picked != null) {
                VoiceTextField(note, { note = it }, testTag = "followUpNote", label = "Note (optional)", minLines = 1)
                PrimaryButton("Save", { onAnswer(f, picked, note) })
            }
        }
    }
}
