package com.mmatthews.process.data

import androidx.room.TypeConverter
import com.mmatthews.process.domain.ActionType
import com.mmatthews.process.domain.EntryType
import com.mmatthews.process.domain.PredictionOutcome
import com.mmatthews.process.domain.ReframeType
import com.mmatthews.process.domain.ScoreType

/** Enums are stored by name so queries can say e.g. actionType = 'PREDICTION'. */
class Converters {
    @TypeConverter fun fromEntryType(v: EntryType): String = v.name
    @TypeConverter fun toEntryType(v: String): EntryType = EntryType.valueOf(v)

    @TypeConverter fun fromReframe(v: ReframeType): String = v.name
    @TypeConverter fun toReframe(v: String): ReframeType = ReframeType.valueOf(v)

    @TypeConverter fun fromAction(v: ActionType): String = v.name
    @TypeConverter fun toAction(v: String): ActionType = ActionType.valueOf(v)

    @TypeConverter fun fromOutcome(v: PredictionOutcome?): String? = v?.name
    @TypeConverter fun toOutcome(v: String?): PredictionOutcome? = v?.let { PredictionOutcome.valueOf(it) }

    @TypeConverter fun fromScore(v: ScoreType): String = v.name
    @TypeConverter fun toScore(v: String): ScoreType = ScoreType.valueOf(v)
}
