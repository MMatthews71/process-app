package com.mmatthews.process.ui.process

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mmatthews.process.data.EntryRepository
import com.mmatthews.process.data.SettingsRepository
import com.mmatthews.process.domain.ActionType
import com.mmatthews.process.domain.DraftRules
import com.mmatthews.process.domain.Flow
import com.mmatthews.process.domain.HelperPicker
import com.mmatthews.process.domain.PredictionRules
import com.mmatthews.process.domain.ProcessDraft
import com.mmatthews.process.domain.ReframeType
import com.mmatthews.process.domain.Step
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Clock
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class ProcessViewModel @Inject constructor(
    private val settingsRepo: SettingsRepository,
    private val entries: EntryRepository,
    private val clock: Clock,
) : ViewModel(), ProcessActions {

    private val _ui = MutableStateFlow<ProcessUi?>(null)
    val ui: StateFlow<ProcessUi?> = _ui.asStateFlow()

    private val exitChannel = Channel<Unit>(Channel.CONFLATED)
    /** Emits when the user backs out of screen 1. */
    val exit = exitChannel.receiveAsFlow()

    private val draftToSave = MutableStateFlow<ProcessDraft?>(null)
    private val dbLock = Mutex()

    init {
        viewModelScope.launch {
            val settings = settingsRepo.settings.first()
            val now = clock.millis()
            val saved = settingsRepo.loadDraft()
            val draft = if (saved != null && DraftRules.shouldResume(saved, now)) saved
            else ProcessDraft(startedAt = now, reframeTab = settings.lastReframeTab)
            _ui.value = ProcessUi(
                draft = draft,
                settings = settings,
                lastCue = cueFor(draft),
                predictionLockedUntil = lockFor(draft),
            )
            draftToSave.value = draft
        }
        // Auto-save the draft; a finished draft is cleared instead.
        viewModelScope.launch {
            draftToSave.debounce(250).collect { d ->
                when {
                    d == null -> Unit
                    d.step == Step.DONE -> settingsRepo.clearDraft()
                    else -> settingsRepo.saveDraft(d)
                }
            }
        }
    }

    private val draft: ProcessDraft? get() = _ui.value?.draft

    private fun update(transform: (ProcessDraft) -> ProcessDraft) {
        val current = _ui.value ?: return
        val next = transform(current.draft)
        if (next == current.draft) return
        val stamped = next.copy(updatedAt = clock.millis())
        _ui.update { it?.copy(draft = stamped) }
        draftToSave.value = stamped
    }

    /** Writes the whole draft to its single Entry row (insert first time, update after). */
    private fun persistEntry() {
        viewModelScope.launch {
            dbLock.withLock {
                val d = draft ?: return@withLock
                val id = entries.saveProcess(d, clock.millis())
                if (d.savedEntryId == null) {
                    _ui.update { it?.copy(draft = it.draft.copy(savedEntryId = id)) }
                    draftToSave.value = draft
                }
            }
        }
    }

    private suspend fun cueFor(d: ProcessDraft): String? =
        d.storyChip?.let { entries.libraryCue(it, writtenBefore = d.startedAt) }

    private suspend fun lockFor(d: ProcessDraft): Long? = PredictionRules.nextAvailableAt(
        lastOtherPredictionAt = entries.latestPredictionAt(excludeId = d.savedEntryId),
        thisEventHasPrediction = d.savedEntryId != null && d.actionType == ActionType.PREDICTION,
        now = clock.millis(),
    )

    private fun pickHelper(): String? {
        val s = _ui.value?.settings ?: return null
        return HelperPicker.pick(s.helpers, s.lastHelper)
    }

    // ---- Navigation ----

    override fun back() {
        val d = draft ?: return
        val prev = Flow.back(d)
        if (prev == null) exitChannel.trySend(Unit) else update { prev }
    }

    override fun skip() {
        val d = draft ?: return
        update { Flow.skip(it, pickHelper()) }
        if (d.step == Step.ACT || d.step == Step.RERATE) persistEntry()
    }

    // ---- Screen 1 ----

    override fun selectBroad(broad: String) = update { Flow.selectBroad(it, broad) }
    override fun togglePrecise(word: String) = update { Flow.togglePrecise(it, word) }
    override fun setIntensity(value: Int) = update { Flow.setIntensity(it, value) }
    override fun nameItNext() = update { Flow.nameItNext(it) }

    // ---- Screen 2 ----

    override fun bodyDone(breathSeconds: Int) = update { Flow.bodyDone(it, breathSeconds) }

    // ---- Screen 3 ----

    override fun setWhatHappened(text: String) = update { Flow.setWhatHappened(it, text) }
    override fun whatNext() = update { Flow.whatNext(it) }

    override fun selectChip(chip: String) {
        update { Flow.selectChip(it, chip) }
        viewModelScope.launch {
            val d = draft ?: return@launch
            val cue = cueFor(d)
            _ui.update { it?.copy(lastCue = cue) }
        }
    }

    override fun setTab(tab: ReframeType) = update { Flow.setTab(it, tab) }
    override fun setNewFact(text: String) = update { it.copy(newFact = text) }
    override fun setTruerExtra(text: String) = update { it.copy(truerExtra = text) }
    override fun setFriendText(text: String) = update { it.copy(friendText = text) }

    override fun reframeNext() {
        val d = draft ?: return
        if (!Flow.canSaveReframe(d)) return
        update { Flow.reframeNext(it) }
        viewModelScope.launch { settingsRepo.setLastReframeTab(d.reframeTab) }
    }

    // ---- Screen 4 ----

    override fun chooseAction(type: ActionType) {
        if (type == ActionType.PREDICTION && _ui.value?.predictionLockedUntil != null) return
        update { Flow.chooseAction(it, type) }
        if (type == ActionType.THERAPY) persistEntry()
    }

    override fun repairAnswer(concreteHarm: Boolean) = update { Flow.repairAnswer(it, concreteHarm) }
    override fun setRepairNote(text: String) = update { it.copy(repairNote = text) }
    override fun setPredictionText(text: String) = update { it.copy(predictionText = text) }
    override fun setPredictionPercent(value: Int) = update { it.copy(predictionPercent = value.coerceIn(0, 100)) }
    override fun setIfCue(text: String) = update { it.copy(ifCue = text) }
    override fun setIfAction(text: String) = update { it.copy(ifAction = text) }

    override fun saveAction() {
        val d = draft ?: return
        if (!Flow.canSaveAction(d)) return
        update { Flow.saveAction(it) }
        persistEntry()
    }

    // ---- Ending ----

    override fun setAfter(value: Int) = update { Flow.setAfter(it, value) }

    override fun rerateNext() {
        update { Flow.rerateNext(it, pickHelper()) }
        persistEntry()
    }

    override fun helperAnswer(done: Boolean) {
        val helper = draft?.helper
        update { Flow.helperAnswer(it, done) }
        persistEntry()
        if (helper != null) viewModelScope.launch { settingsRepo.setLastHelper(helper) }
    }
}
