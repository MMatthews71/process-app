package com.mmatthews.process.domain

import kotlinx.serialization.Serializable

/** The four screens, then the ending (re-rate, helper, done). */
enum class Step { NAME_IT, BODY, STORY, ACT, RERATE, HELPER, DONE }

enum class StoryStage { WHAT, CHIP, REFRAME }

enum class ActStage { CHOOSE, LET_GO, REPAIR_ASK, REPAIR_YES, REPAIR_NO, PREDICTION, IF_THEN }

/**
 * The in-progress PROCESS session. Auto-saved to DataStore after every change so
 * the flow can resume within [DraftRules.RESUME_WINDOW_MS].
 */
@Serializable
data class ProcessDraft(
    val startedAt: Long,
    val updatedAt: Long = startedAt,
    val step: Step = Step.NAME_IT,
    val storyStage: StoryStage = StoryStage.WHAT,
    val actStage: ActStage = ActStage.CHOOSE,

    // Screen 1
    val feelingBroad: String? = null,
    val feelingPrecise: String? = null,
    val feelingPrecise2: String? = null,
    val intensitySlider: Int = Defaults.INTENSITY,
    val intensityBefore: Int? = null,

    // Screen 2
    val breathSeconds: Int = 0,

    // Screen 3
    val whatHappened: String = "",
    val storyChip: String? = null,
    val reframeTab: ReframeType = ReframeType.TRUER,
    val newFact: String = "",
    val truerExtra: String = "",
    val friendText: String = "",
    val reframeUsed: ReframeType = ReframeType.NONE,

    // Screen 4
    val actionType: ActionType = ActionType.NONE,
    val repairNote: String = "",
    val predictionText: String = "",
    val predictionPercent: Int = Defaults.PREDICTION_PERCENT,
    val ifCue: String = "",
    val ifAction: String = "",

    // Ending
    val savedEntryId: Long? = null,
    val afterSlider: Int? = null,
    val intensityAfter: Int? = null,
    val helper: String? = null,
    val helperDone: Boolean? = null,
)
