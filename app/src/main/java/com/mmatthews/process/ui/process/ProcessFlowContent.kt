package com.mmatthews.process.ui.process

import androidx.compose.runtime.Composable
import com.mmatthews.process.domain.Step

@Composable
fun ProcessFlowContent(ui: ProcessUi, actions: ProcessActions, onFinished: () -> Unit) {
    val d = ui.draft
    when (d.step) {
        Step.NAME_IT -> NameItScreen(d, ui.settings.feelings, actions)
        Step.BODY -> BodyScreen(
            cycles = ui.settings.breathCycles,
            onBack = actions::back,
            onDone = actions::bodyDone,
        )
        Step.STORY -> StoryScreen(ui, actions)
        Step.ACT -> ActScreen(ui, actions)
        Step.RERATE -> RerateScreen(d, actions)
        Step.HELPER -> HelperScreen(d.helper.orEmpty(), actions)
        Step.DONE -> DoneScreen(onFinished)
    }
}
