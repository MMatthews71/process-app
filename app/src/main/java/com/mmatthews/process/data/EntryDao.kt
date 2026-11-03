package com.mmatthews.process.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EntryDao {
    @Insert
    suspend fun insert(entry: Entry): Long

    @Query("SELECT * FROM entries WHERE outcome IS NULL ORDER BY createdAt ASC")
    suspend fun unanswered(): List<Entry>

    @Query("UPDATE entries SET outcome = :outcome WHERE id = :id")
    suspend fun setOutcome(id: Long, outcome: String)

    @Query("SELECT * FROM entries WHERE rule IS NOT NULL ORDER BY createdAt DESC")
    fun rules(): Flow<List<Entry>>

    /** Removes the rule; the entry itself stays so its next-day check still happens. */
    @Query("UPDATE entries SET rule = NULL WHERE id = :id")
    suspend fun deleteRule(id: Long)
}
