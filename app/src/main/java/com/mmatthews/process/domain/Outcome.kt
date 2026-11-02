package com.mmatthews.process.domain

/** The values a draft writes to its Entry row. */
data class ProcessOutcome(
    val feelingBroad: String?,
    val feelingPrecise: String?,
    val feelingPrecise2: String?,
    val intensityBefore: Int?,
    val intensityAfter: Int?,
    val whatHappened: String,
    val storyChip: String?,
    val reframeType: ReframeType,
    val reframeText: String,
    val newFact: String,
    val actionType: ActionType,
    val actionNote: String,
    val ifCue: String,
    val ifAction: String,
    val predictionText: String,
    val predictionPercent: Int?,
    val helperSuggested: String?,
    val helperDone: Boolean?,
    val flaggedTherapy: Boolean,
    val breathSeconds: Int,
    /** Text saved to StoryLibrary for the chip, shown next time as "Last time you wrote". */
    val libraryText: String?,
)

fun ProcessDraft.outcome(): ProcessOutcome {
    val reframeType = reframeUsed
    val reframeText = when (reframeType) {
        ReframeType.TRUER -> truerExtra.trim()
        ReframeType.FRIEND -> friendText.trim()
        ReframeType.NONE -> ""
    }
    val fact = if (reframeType == ReframeType.TRUER) newFact.trim() else ""
    val library = when (reframeType) {
        ReframeType.TRUER -> listOf(fact, reframeText).filter { it.isNotEmpty() }.joinToString(" · ")
        ReframeType.FRIEND -> reframeText
        ReframeType.NONE -> ""
    }.takeIf { it.isNotEmpty() && storyChip != null }

    val action = actionType
    return ProcessOutcome(
        feelingBroad = feelingBroad,
        feelingPrecise = feelingPrecise,
        feelingPrecise2 = feelingPrecise2,
        intensityBefore = intensityBefore,
        intensityAfter = intensityAfter,
        whatHappened = whatHappened.trim(),
        storyChip = storyChip,
        reframeType = reframeType,
        reframeText = reframeText,
        newFact = fact,
        actionType = action,
        actionNote = if (action == ActionType.REPAIR) repairNote.trim() else "",
        ifCue = if (action == ActionType.IF_THEN) ifCue.trim() else "",
        ifAction = if (action == ActionType.IF_THEN) ifAction.trim() else "",
        predictionText = if (action == ActionType.PREDICTION) predictionText.trim() else "",
        predictionPercent = if (action == ActionType.PREDICTION) predictionPercent else null,
        helperSuggested = helper,
        helperDone = helperDone,
        flaggedTherapy = action == ActionType.THERAPY,
        breathSeconds = breathSeconds,
        libraryText = library,
    )
}
