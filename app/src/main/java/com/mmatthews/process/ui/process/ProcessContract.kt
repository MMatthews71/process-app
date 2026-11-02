package com.mmatthews.process.ui.process

import com.mmatthews.process.domain.ActionType
import com.mmatthews.process.domain.AppSettings
import com.mmatthews.process.domain.ProcessDraft
import com.mmatthews.process.domain.ReframeType

data class ProcessUi(
    val draft: ProcessDraft,
    val settings: AppSettings,
    /** "Last time you wrote" text for the selected chip, if any. */
    val lastCue: String? = null,
    /** If set, "Test the prediction" is greyed out until this time. */
    val predictionLockedUntil: Long? = null,
)

interface ProcessActions {
    fun back()
    fun skip()

    fun selectBroad(broad: String)
    fun togglePrecise(word: String)
    fun setIntensity(value: Int)
    fun nameItNext()

    fun bodyDone(breathSeconds: Int)

    fun setWhatHappened(text: String)
    fun whatNext()
    fun selectChip(chip: String)
    fun setTab(tab: ReframeType)
    fun setNewFact(text: String)
    fun setTruerExtra(text: String)
    fun setFriendText(text: String)
    fun reframeNext()

    fun chooseAction(type: ActionType)
    fun repairAnswer(concreteHarm: Boolean)
    fun setRepairNote(text: String)
    fun setPredictionText(text: String)
    fun setPredictionPercent(value: Int)
    fun setIfCue(text: String)
    fun setIfAction(text: String)
    fun saveAction()

    fun setAfter(value: Int)
    fun rerateNext()
    fun helperAnswer(done: Boolean)
}
