package com.habitgame.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class EntryType { COMMITMENT_DONE, TEMPTATION_SLIP, ADDICTION_SLIP }

@Entity(
    tableName = "daily_log_entries",
    foreignKeys = [ForeignKey(
        entity = DailyLog::class,
        parentColumns = ["id"],
        childColumns = ["dailyLogId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("dailyLogId")]
)
data class DailyLogEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dailyLogId: Int,
    val entryType: EntryType,
    val refName: String,
    val pointDelta: Int
)
