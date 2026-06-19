package com.habitgame.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class CommitmentType { BINARY, COUNT }

@Entity(
    tableName = "commitments",
    foreignKeys = [ForeignKey(
        entity = Challenge::class,
        parentColumns = ["id"],
        childColumns = ["challengeId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("challengeId")]
)
data class Commitment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val challengeId: Int,
    val name: String,
    val pointValue: Int,
    val type: CommitmentType = CommitmentType.BINARY,
    val maxCount: Int = 1,
    val active: Boolean = true
)
