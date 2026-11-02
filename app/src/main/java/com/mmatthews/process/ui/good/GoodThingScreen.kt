package com.mmatthews.process.ui.good

import androidx.compose.runtime.Composable
import com.mmatthews.process.ui.components.FlowScaffold
import com.mmatthews.process.ui.components.PrimaryButton
import com.mmatthews.process.ui.components.Prompt
import com.mmatthews.process.ui.components.VoiceTextField

@Composable
fun GoodThingScreen(text: String, onText: (String) -> Unit, onSave: () -> Unit, onBack: () -> Unit) {
    FlowScaffold(
        onBack = onBack,
        onSkip = null,
        bottom = { PrimaryButton("Save", onSave, enabled = text.isNotBlank()) },
    ) {
        Prompt("What went well?")
        VoiceTextField(text, onText, testTag = "goodThing", minLines = 3)
    }
}
