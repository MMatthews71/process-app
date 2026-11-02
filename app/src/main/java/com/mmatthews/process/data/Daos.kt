package com.mmatthews.process.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface EntryDao {
    @Insert
    suspend fun insert(entry: Entry): Long

    @Update
    suspend fun update(entry: Entry)

    @Query("SELECT * FROM entries WHERE id = :id")
    suspend fun get(id: Long): Entry?

    @Query("SELECT * FROM entries ORDER BY createdAt DESC")
    suspend fun all(): List<Entry>

    @Query("SELECT MAX(createdAt) FROM entries WHERE actionType = 'PREDICTION' AND id != :excludeId")
    suspend fun latestPredictionAt(excludeId: Long): Long?

    @Query("SELECT * FROM entries WHERE actionType = 'PREDICTION' AND predictionOutcome IS NULL ORDER BY createdAt ASC")
    fun pendingPredictions(): Flow<List<Entry>>

    @Query("UPDATE entries SET predictionOutcome = :outcome, actionNote = :note WHERE id = :id")
    suspend fun setPredictionOutcome(id: Long, outcome: String, note: String)
}

@Dao
interface StoryLibraryDao {
    @Upsert
    suspend fun upsert(item: StoryLibrary)

    @Query("SELECT * FROM story_library WHERE storyChip = :chip")
    suspend fun get(chip: String): StoryLibrary?
}

@Dao
interface ScoreLogDao {
    @Insert
    suspend fun insert(item: ScoreLog): Long

    @Query("SELECT * FROM score_log ORDER BY date DESC")
    suspend fun all(): List<ScoreLog>
}
