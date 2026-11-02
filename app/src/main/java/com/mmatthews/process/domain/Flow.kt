package com.mmatthews.process.domain

/**
 * Pure state transitions for the PROCESS flow. The ViewModel applies these and
 * handles saving; keeping them here makes the flow testable on the JVM.
 */
object Flow {

    // ---- Screen 1: Name it ----

    fun selectBroad(d: ProcessDraft, broad: String): ProcessDraft =
        if (d.feelingBroad == broad) d
        else d.copy(feelingBroad = broad, feelingPrecise = null, feelingPrecise2 = null)

    /** First tap sets the main word, a second word is the optional extra; tapping again removes it. */
    fun togglePrecise(d: ProcessDraft, word: String): ProcessDraft = when (word) {
        d.feelingPrecise -> d.copy(feelingPrecise = d.feelingPrecise2, feelingPrecise2 = null)
        d.feelingPrecise2 -> d.copy(feelingPrecise2 = null)
        else -> when {
            d.feelingPrecise == null -> d.copy(feelingPrecise = word)
            else -> d.copy(feelingPrecise2 = word)
        }
    }

    fun setIntensity(d: ProcessDraft, value: Int) = d.copy(intensitySlider = value.coerceIn(0, 10))

    fun nameItNext(d: ProcessDraft) = d.copy(intensityBefore = d.intensitySlider, step = Step.BODY)

    // ---- Screen 2: Body ----

    fun bodyDone(d: ProcessDraft, breathSeconds: Int) =
        d.copy(breathSeconds = breathSeconds, step = Step.STORY, storyStage = StoryStage.WHAT)

    // ---- Screen 3: Story ----

    fun setWhatHappened(d: ProcessDraft, text: String) = d.copy(whatHappened = text)

    fun whatNext(d: ProcessDraft) = d.copy(whatHappened = d.whatHappened.trim(), storyStage = StoryStage.CHIP)

    fun selectChip(d: ProcessDraft, chip: String) = d.copy(storyChip = chip, storyStage = StoryStage.REFRAME)

    fun setTab(d: ProcessDraft, tab: ReframeType) = d.copy(reframeTab = tab)

    fun canSaveReframe(d: ProcessDraft): Boolean = when (d.reframeTab) {
        ReframeType.TRUER -> TextRules.wordCount(d.newFact) >= TextRules.MIN_NEW_FACT_WORDS
        ReframeType.FRIEND -> d.friendText.isNotBlank()
        ReframeType.NONE -> false
    }

    fun reframeNext(d: ProcessDraft): ProcessDraft =
        if (!canSaveReframe(d)) d
        else d.copy(reframeUsed = d.reframeTab, step = Step.ACT, actStage = ActStage.CHOOSE)

    // ---- Screen 4: Act ----

    /** Opens the sub-screen for an action. THERAPY needs no input, so it commits straight away. */
    fun chooseAction(d: ProcessDraft, type: ActionType): ProcessDraft = when (type) {
        ActionType.LET_GO -> d.copy(actStage = ActStage.LET_GO)
        ActionType.REPAIR -> d.copy(actStage = ActStage.REPAIR_ASK)
        ActionType.PREDICTION -> d.copy(
            actStage = ActStage.PREDICTION,
            predictionText = d.predictionText.ifBlank { d.storyChip.orEmpty() },
        )
        ActionType.IF_THEN -> d.copy(actStage = ActStage.IF_THEN)
        ActionType.THERAPY -> d.copy(actionType = ActionType.THERAPY, step = Step.RERATE)
        ActionType.NONE -> d
    }

    fun repairAnswer(d: ProcessDraft, concreteHarm: Boolean) =
        d.copy(actStage = if (concreteHarm) ActStage.REPAIR_YES else ActStage.REPAIR_NO)

    fun canSaveAction(d: ProcessDraft): Boolean = when (d.actStage) {
        ActStage.LET_GO, ActStage.REPAIR_NO -> true
        ActStage.REPAIR_YES -> d.repairNote.isNotBlank()
        ActStage.PREDICTION -> d.predictionText.isNotBlank()
        ActStage.IF_THEN -> d.ifCue.isNotBlank() && d.ifAction.isNotBlank()
        ActStage.CHOOSE, ActStage.REPAIR_ASK -> false
    }

    fun saveAction(d: ProcessDraft): ProcessDraft {
        if (!canSaveAction(d)) return d
        val type = when (d.actStage) {
            ActStage.LET_GO, ActStage.REPAIR_NO -> ActionType.LET_GO
            ActStage.REPAIR_YES -> ActionType.REPAIR
            ActStage.PREDICTION -> ActionType.PREDICTION
            ActStage.IF_THEN -> ActionType.IF_THEN
            else -> return d
        }
        return d.copy(actionType = type, step = Step.RERATE)
    }

    // ---- Ending ----

    fun setAfter(d: ProcessDraft, value: Int) = d.copy(afterSlider = value.coerceIn(0, 10))

    fun afterSliderValue(d: ProcessDraft): Int = d.afterSlider ?: d.intensityBefore ?: Defaults.INTENSITY

    fun rerateNext(d: ProcessDraft, helper: String?) =
        toHelper(d.copy(intensityAfter = afterSliderValue(d)), helper)

    fun helperAnswer(d: ProcessDraft, done: Boolean) = d.copy(helperDone = done, step = Step.DONE)

    private fun toHelper(d: ProcessDraft, helper: String?): ProcessDraft {
        val chosen = d.helper ?: helper
        return if (chosen == null) d.copy(step = Step.DONE) else d.copy(helper = chosen, step = Step.HELPER)
    }

    // ---- Skip & back ----

    /** Skips the current step. [helper] is used when skipping into the helper suggestion. */
    fun skip(d: ProcessDraft, helper: String?): ProcessDraft = when (d.step) {
        Step.NAME_IT -> d.copy(intensityBefore = null, step = Step.BODY)
        Step.BODY -> d.copy(step = Step.STORY, storyStage = StoryStage.WHAT)
        Step.STORY -> when (d.storyStage) {
            StoryStage.WHAT -> d.copy(whatHappened = "", storyStage = StoryStage.CHIP)
            StoryStage.CHIP -> d.copy(storyChip = null, storyStage = StoryStage.REFRAME)
            StoryStage.REFRAME -> d.copy(reframeUsed = ReframeType.NONE, step = Step.ACT, actStage = ActStage.CHOOSE)
        }
        Step.ACT -> d.copy(actionType = ActionType.NONE, step = Step.RERATE)
        Step.RERATE -> toHelper(d.copy(intensityAfter = null), helper)
        Step.HELPER, Step.DONE -> d
    }

    /** Returns the previous state, or null to leave the flow (draft stays saved). */
    fun back(d: ProcessDraft): ProcessDraft? = when (d.step) {
        Step.NAME_IT -> null
        Step.BODY -> d.copy(step = Step.NAME_IT)
        Step.STORY -> when (d.storyStage) {
            StoryStage.WHAT -> d.copy(step = Step.BODY)
            StoryStage.CHIP -> d.copy(storyStage = StoryStage.WHAT)
            StoryStage.REFRAME -> d.copy(storyStage = StoryStage.CHIP)
        }
        Step.ACT -> when (d.actStage) {
            ActStage.CHOOSE -> d.copy(step = Step.STORY, storyStage = StoryStage.REFRAME)
            ActStage.REPAIR_YES, ActStage.REPAIR_NO -> d.copy(actStage = ActStage.REPAIR_ASK)
            else -> d.copy(actStage = ActStage.CHOOSE)
        }
        Step.RERATE -> d.copy(step = Step.ACT, actStage = ActStage.CHOOSE)
        Step.HELPER -> d.copy(step = Step.RERATE)
        Step.DONE -> d
    }
}
