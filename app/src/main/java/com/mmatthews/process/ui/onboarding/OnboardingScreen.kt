package com.mmatthews.process.ui.onboarding

import androidx.compose.runtime.Composable
import com.mmatthews.process.ui.components.FlowScaffold
import com.mmatthews.process.ui.components.PrimaryButton
import com.mmatthews.process.ui.components.Prompt
import com.mmatthews.process.ui.components.SecondaryButton
import com.mmatthews.process.ui.components.VoiceTextField

enum class OnboardingPage { NAME, ABOUT, NOTIFICATIONS }

@Composable
fun OnboardingScreen(
    page: OnboardingPage,
    name: String,
    onName: (String) -> Unit,
    onNext: () -> Unit,
    onBack: (() -> Unit)?,
    onAllowNotifications: () -> Unit,
    onSkipNotifications: () -> Unit,
) {
    when (page) {
        OnboardingPage.NAME -> FlowScaffold(
            onBack = null,
            onSkip = null,
            bottom = { PrimaryButton("Next", onNext, enabled = name.isNotBlank()) },
        ) {
            Prompt("What's your first name?")
            VoiceTextField(name, onName, testTag = "name", singleLine = true)
        }

        OnboardingPage.ABOUT -> FlowScaffold(
            onBack = onBack,
            onSkip = null,
            bottom = { PrimaryButton("Got it", onNext) },
        ) {
            Prompt(
                "Use this after something happens, not during or before a social situation. " +
                    "It won't make the feeling disappear, and it doesn't need to.",
            )
        }

        OnboardingPage.NOTIFICATIONS -> FlowScaffold(
            onBack = onBack,
            onSkip = null,
            bottom = {
                PrimaryButton("Allow notifications", onAllowNotifications)
                SecondaryButton("Not now", onSkipNotifications)
            },
        ) {
            Prompt("The morning after you use this, one notification asks how much it replayed.")
        }
    }
}
