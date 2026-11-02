package com.mmatthews.process.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mmatthews.process.domain.ActionType
import com.mmatthews.process.domain.EntryType
import com.mmatthews.process.domain.PredictionOutcome
import com.mmatthews.process.domain.ReframeType
import com.mmatthews.process.domain.ScoreType

@Entity(
    tableName = "entries",
    indices = [Index("createdAt"), Index("memoryGroupId"), Index("storyChip")],
)
data class Entry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: EntryType,
    val createdAt: Long,
    val feelingBroad: String? = null,
    val feelingPrecise: String? = null,
    val feelingPrecise2: String? = null,
    /** Null when screen 1 was skipped. */
    val intensityBefore: Int? = null,
    val intensityAfter: Int? = null,
    val whatHappened: String = "",
    val storyChip: String? = null,
    val reframeType: ReframeType = ReframeType.NONE,
    val reframeText: String = "",
    val newFact: String = "",
    val actionType: ActionType = ActionType.NONE,
    /** Repair: "What will I do, and when?". Prediction: the optional follow-up note. */
    val actionNote: String = "",
    val ifCue: String = "",
    val ifAction: String = "",
    val predictionText: String = "",
    val predictionPercent: Int? = null,
    val predictionOutcome: PredictionOutcome? = null,
    val helperSuggested: String? = null,
    val helperDone: Boolean? = null,
    val morningReplay: Int? = null,
    val linkedEntryId: Long? = null,
    val memoryGroupId: String? = null,
    val flaggedTherapy: Boolean = false,
    val discussedInTherapy: Boolean = false,
    val breathSeconds: Int = 0,
    val durationSeconds: Int = 0,
    val exportedPath: String? = null,
    /** For "archive" on the Lessons screen (step 4). */
    val archived: Boolean = false,
)

@Entity(tableName = "story_library")
data class StoryLibrary(
    @PrimaryKey val storyChip: String,
    val latestReframe: String,
    val updatedAt: Long,
)

@Entity(tableName = "score_log")
data class ScoreLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long,
    val type: ScoreType,
    val score: Int,
)
