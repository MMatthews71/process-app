package com.mmatthews.process.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [Entry::class, StoryLibrary::class, ScoreLog::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun entryDao(): EntryDao
    abstract fun storyLibraryDao(): StoryLibraryDao
    abstract fun scoreLogDao(): ScoreLogDao
}
