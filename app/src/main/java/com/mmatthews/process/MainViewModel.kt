package com.mmatthews.process

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mmatthews.process.data.SettingsRepository
import com.mmatthews.process.domain.DraftRules
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Clock
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val settings: SettingsRepository,
    private val clock: Clock,
) : ViewModel() {

    private val _start = MutableStateFlow<String?>(null)
    val startRoute: StateFlow<String?> = _start.asStateFlow()

    /** Onboarding first; then straight into the flow if asked to, or if a recent draft exists. */
    fun resolveStart(openProcess: Boolean) {
        if (_start.value != null) return
        viewModelScope.launch {
            val s = settings.settings.first()
            val draft = settings.loadDraft()
            _start.value = when {
                !s.onboarded -> Routes.ONBOARDING
                openProcess || DraftRules.shouldResume(draft, clock.millis()) -> Routes.PROCESS
                else -> Routes.HOME
            }
        }
    }
}
