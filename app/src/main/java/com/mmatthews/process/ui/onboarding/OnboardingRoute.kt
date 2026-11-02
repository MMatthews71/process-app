package com.mmatthews.process.ui.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mmatthews.process.data.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(private val settings: SettingsRepository) : ViewModel() {
    fun finish(name: String, then: () -> Unit) {
        viewModelScope.launch {
            settings.completeOnboarding(name)
            then()
        }
    }
}

@Composable
fun OnboardingRoute(onDone: () -> Unit, vm: OnboardingViewModel = hiltViewModel()) {
    var page by rememberSaveable { mutableStateOf(OnboardingPage.NAME) }
    var name by rememberSaveable { mutableStateOf("") }
    val askNotifications = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    val finish = { vm.finish(name, onDone) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { finish() }

    BackHandler(enabled = page != OnboardingPage.NAME) {
        page = OnboardingPage.entries[page.ordinal - 1]
    }

    OnboardingScreen(
        page = page,
        name = name,
        onName = { name = it },
        onNext = {
            when (page) {
                OnboardingPage.NAME -> page = OnboardingPage.ABOUT
                OnboardingPage.ABOUT -> if (askNotifications) page = OnboardingPage.NOTIFICATIONS else finish()
                OnboardingPage.NOTIFICATIONS -> finish()
            }
        },
        onBack = { page = OnboardingPage.entries[(page.ordinal - 1).coerceAtLeast(0)] },
        onAllowNotifications = {
            if (askNotifications) launcher.launch(Manifest.permission.POST_NOTIFICATIONS) else finish()
        },
        onSkipNotifications = finish,
    )
}
