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
    fun finish(then: () -> Unit) {
        viewModelScope.launch {
            settings.completeOnboarding()
            then()
        }
    }
}

@Composable
fun OnboardingRoute(onDone: () -> Unit, vm: OnboardingViewModel = hiltViewModel()) {
    var page by rememberSaveable { mutableStateOf(OnboardingPage.ABOUT) }
    val askNotifications = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    val finish = { vm.finish(onDone) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { finish() }

    BackHandler(enabled = page == OnboardingPage.NOTIFICATIONS) { page = OnboardingPage.ABOUT }

    OnboardingScreen(
        page = page,
        onNext = { if (askNotifications) page = OnboardingPage.NOTIFICATIONS else finish() },
        onBack = { page = OnboardingPage.ABOUT },
        onAllowNotifications = {
            if (askNotifications) launcher.launch(Manifest.permission.POST_NOTIFICATIONS) else finish()
        },
        onSkipNotifications = finish,
    )
}
