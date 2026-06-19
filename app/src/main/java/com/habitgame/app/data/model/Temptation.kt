package com.habitgame.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "temptations",
    foreignKeys = [ForeignKey(
        entity = Challenge::class,
        parentColumns = ["id"],
        childColumns = ["challengeId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("challengeId")]
)
data class Temptation(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val challengeId: Int,
    val name: String,
    val unlockThreshold: Int,
    val slipPenalty: Int,
    val active: Boolean = true
)
