package com.mmatthews.process.ui.process

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mmatthews.process.domain.Flow
import com.mmatthews.process.domain.ProcessDraft
import com.mmatthews.process.ui.components.CrisisFooter
import com.mmatthews.process.ui.components.FlowScaffold
import com.mmatthews.process.ui.components.IntensitySlider
import com.mmatthews.process.ui.components.PrimaryButton
import com.mmatthews.process.ui.components.Prompt
import com.mmatthews.process.ui.components.SecondaryButton
import kotlinx.coroutines.delay

@Composable
fun RerateScreen(d: ProcessDraft, a: ProcessActions) {
    FlowScaffold(onBack = a::back, onSkip = a::skip, bottom = { PrimaryButton("Next", a::rerateNext) }) {
        Prompt("How strong now? Optional")
        IntensitySlider(Flow.afterSliderValue(d), a::setAfter)
    }
}

@Composable
fun HelperScreen(helper: String, a: ProcessActions) {
    FlowScaffold(
        onBack = a::back,
        onSkip = null,
        bottom = {
            PrimaryButton("Done it", { a.helperAnswer(true) })
            SecondaryButton("Not now", { a.helperAnswer(false) })
        },
    ) {
        Spacer(Modifier.height(48.dp))
        Text(
            helper,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

const val DONE_HOLD_MS = 2_500L

/** "Done. Back to your day." then the app closes. */
@Composable
fun DoneScreen(onFinished: () -> Unit) {
    val finish by rememberUpdatedState(onFinished)
    LaunchedEffect(Unit) {
        delay(DONE_HOLD_MS)
        finish()
    }
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp), verticalArrangement = Arrangement.Center) {
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(
                "Done. Back to your day.",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
            )
        }
        CrisisFooter()
    }
}

