package com.habitgame.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class ActionType { COMMITMENT, TEMPTATION_SLIP, ADDICTION_SLIP }

@Entity(
    tableName = "today_actions",
    foreignKeys = [ForeignKey(
        entity = Challenge::class,
        parentColumns = ["id"],
        childColumns = ["challengeId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("challengeId")]
)
data class TodayAction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val challengeId: Int,
    val actionType: ActionType,
    val refId: Int = 0,
    val refName: String,
    val pointDelta: Int,
    val count: Int = 1,
    val date: Long
)
