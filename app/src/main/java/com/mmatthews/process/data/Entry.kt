package com.mmatthews.process.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter

enum class Outcome { YES, NO, UNKNOWN }

@Entity(tableName = "entries")
data class Entry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val createdAt: Long,
    val happened: String,
    val story: String,
    /** Answer from the next-day check. Null until answered. */
    val outcome: Outcome? = null,
    /** "Next time I'll …" text. Null means "let it go". */
    val rule: String? = null,
)

class Converters {
    @TypeConverter
    fun fromOutcome(value: Outcome?): String? = value?.name

    @TypeConverter
    fun toOutcome(value: String?): Outcome? = value?.let { Outcome.valueOf(it) }
}

object Stories {
    const val OTHER = "Other"

    val all = listOf(
        "They think I'm weird",
        "I'm not good enough",
        "They don't like me",
        "I'll be rejected",
        "I hurt someone",
        OTHER,
    )
}
