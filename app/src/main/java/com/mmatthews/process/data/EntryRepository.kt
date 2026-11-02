package com.mmatthews.process.data

import androidx.room.withTransaction
import com.mmatthews.process.domain.EntryType
import com.mmatthews.process.domain.PredictionOutcome
import com.mmatthews.process.domain.ProcessDraft
import com.mmatthews.process.domain.outcome
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EntryRepository @Inject constructor(private val db: AppDatabase) {
    private val entries = db.entryDao()
    private val library = db.storyLibraryDao()

    /**
     * Writes the draft as one PROCESS entry: inserts on first save, updates after that
     * (so going back and changing the action never creates a second entry).
     */
    suspend fun saveProcess(draft: ProcessDraft, now: Long): Long = db.withTransaction {
        val o = draft.outcome()
        val existing = draft.savedEntryId?.let { entries.get(it) }
        val base = existing ?: Entry(
            type = EntryType.PROCESS,
            createdAt = draft.startedAt,
            memoryGroupId = UUID.randomUUID().toString(),
        )
        val entry = base.copy(
            feelingBroad = o.feelingBroad,
            feelingPrecise = o.feelingPrecise,
            feelingPrecise2 = o.feelingPrecise2,
            intensityBefore = o.intensityBefore,
            intensityAfter = o.intensityAfter,
            whatHappened = o.whatHappened,
            storyChip = o.storyChip,
            reframeType = o.reframeType,
            reframeText = o.reframeText,
            newFact = o.newFact,
            actionType = o.actionType,
            actionNote = o.actionNote,
            ifCue = o.ifCue,
            ifAction = o.ifAction,
            predictionText = o.predictionText,
            predictionPercent = o.predictionPercent,
            helperSuggested = o.helperSuggested,
            helperDone = o.helperDone,
            flaggedTherapy = o.flaggedTherapy,
            breathSeconds = o.breathSeconds,
            durationSeconds = ((now - draft.startedAt) / 1000).toInt().coerceAtLeast(0),
        )
        val id = if (existing == null) entries.insert(entry) else entry.id.also { entries.update(entry) }
        if (o.storyChip != null && o.libraryText != null) {
            library.upsert(StoryLibrary(o.storyChip, o.libraryText, now))
        }
        id
    }

    suspend fun addGood(text: String, now: Long): Long =
        entries.insert(Entry(type = EntryType.GOOD, createdAt = now, whatHappened = text.trim()))

    /** The "Last time you wrote" cue, ignoring anything written during the current session. */
    suspend fun libraryCue(chip: String, writtenBefore: Long): String? =
        library.get(chip)?.takeIf { it.updatedAt < writtenBefore }?.latestReframe

    suspend fun latestPredictionAt(excludeId: Long?): Long? = entries.latestPredictionAt(excludeId ?: -1L)

    fun pendingPredictions(): Flow<List<Entry>> = entries.pendingPredictions()

    suspend fun setPredictionOutcome(id: Long, outcome: PredictionOutcome, note: String) =
        entries.setPredictionOutcome(id, outcome.name, note.trim())
}
