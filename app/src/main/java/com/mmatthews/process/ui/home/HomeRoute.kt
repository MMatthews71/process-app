package com.mmatthews.process.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.mmatthews.process.data.EntryRepository
import com.mmatthews.process.domain.PredictionOutcome
import com.mmatthews.process.domain.PredictionRules
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Clock
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val entries: EntryRepository,
    private val clock: Clock,
) : ViewModel() {

    /** The oldest prediction that is at least 24 h old and still has no outcome. */
    val followUp: StateFlow<FollowUpUi?> = entries.pendingPredictions()
        .map { list ->
            val now = clock.millis()
            list.firstOrNull { PredictionRules.isFollowUpDue(it.createdAt, it.predictionOutcome, now) }
                ?.let { FollowUpUi(it.id, it.predictionText, it.predictionPercent) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun answer(f: FollowUpUi, outcome: PredictionOutcome, note: String) {
        viewModelScope.launch { entries.setPredictionOutcome(f.entryId, outcome, note) }
    }
}

@Composable
fun HomeRoute(onProcess: () -> Unit, onGood: () -> Unit, vm: HomeViewModel = hiltViewModel()) {
    val followUp by vm.followUp.collectAsStateWithLifecycle()
    HomeScreen(
        followUp = followUp,
        onProcess = onProcess,
        onGood = onGood,
        onAnswerFollowUp = vm::answer,
    )
}
