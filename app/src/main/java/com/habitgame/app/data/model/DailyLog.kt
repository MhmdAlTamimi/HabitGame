package com.habitgame.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "daily_logs",
    foreignKeys = [ForeignKey(
        entity = Challenge::class,
        parentColumns = ["id"],
        childColumns = ["challengeId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("challengeId")]
)
data class DailyLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val challengeId: Int,
    val date: Long,
    val dailyPoints: Int = 0,
    val totalAfterClose: Int = 0,
    val closed: Boolean = false
)
