package com.mmatthews.process.ui.process

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontStyle
import com.mmatthews.process.domain.Flow
import com.mmatthews.process.domain.Prompts
import com.mmatthews.process.domain.ReframeType
import com.mmatthews.process.domain.StoryStage
import com.mmatthews.process.domain.TextRules
import com.mmatthews.process.ui.components.ChoiceButton
import com.mmatthews.process.ui.components.FlowScaffold
import com.mmatthews.process.ui.components.PrimaryButton
import com.mmatthews.process.ui.components.Prompt
import com.mmatthews.process.ui.components.VoiceTextField

/** Screen 3 — Story: what happened, the story chip, then a truer version or friend view. */
@Composable
fun StoryScreen(ui: ProcessUi, a: ProcessActions) {
    val d = ui.draft
    when (d.storyStage) {
        StoryStage.WHAT -> FlowScaffold(
            onBack = a::back,
            onSkip = a::skip,
            bottom = { PrimaryButton("Next", a::whatNext, enabled = d.whatHappened.isNotBlank()) },
        ) {
            Prompt("What happened? One sentence, like a camera would see it.")
            VoiceTextField(d.whatHappened, a::setWhatHappened, testTag = "whatHappened")
        }

        StoryStage.CHIP -> FlowScaffold(onBack = a::back, onSkip = a::skip) {
            Prompt("What's the story?")
            ui.settings.storyChips.forEach { chip ->
                ChoiceButton(chip, selected = chip == d.storyChip, onClick = { a.selectChip(chip) }, fullWidth = true)
            }
        }

        StoryStage.REFRAME -> FlowScaffold(
            onBack = a::back,
            onSkip = a::skip,
            bottom = { PrimaryButton("Next", a::reframeNext, enabled = Flow.canSaveReframe(d)) },
        ) {
            val friend = d.reframeTab == ReframeType.FRIEND
            TabRow(selectedTabIndex = if (friend) 1 else 0) {
                Tab(
                    selected = !friend,
                    onClick = { a.setTab(ReframeType.TRUER) },
                    text = { Text("Truer version", style = MaterialTheme.typography.titleMedium) },
                )
                Tab(
                    selected = friend,
                    onClick = { a.setTab(ReframeType.FRIEND) },
                    text = { Text("Friend", style = MaterialTheme.typography.titleMedium) },
                )
            }
            if (!friend) {
                Prompt(Prompts.truer(ui.settings.name))
                ui.lastCue?.let { cue ->
                    Text(
                        "Last time you wrote: $cue",
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                VoiceTextField(
                    d.newFact,
                    a::setNewFact,
                    testTag = "newFact",
                    label = "One new fact from today or the past year that doesn't fit the story",
                )
                val words = TextRules.wordCount(d.newFact)
                if (words in 1 until TextRules.MIN_NEW_FACT_WORDS) {
                    Text(
                        "At least ${TextRules.MIN_NEW_FACT_WORDS} words.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                VoiceTextField(
                    d.truerExtra,
                    a::setTruerExtra,
                    testTag = "truerExtra",
                    label = "Anything else (optional)",
                    minLines = 1,
                )
            } else {
                Prompt(Prompts.friend(ui.settings.name))
                VoiceTextField(d.friendText, a::setFriendText, testTag = "friendText", minLines = 3)
            }
        }
    }
}
