package com.habitgame.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ChallengeStatus { ACTIVE, ENDED }

@Entity(tableName = "challenges")
data class Challenge(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val addictionName: String,
    val addictionPenaltyPercent: Int,
    val dayCloseHour: Int,
    val dayCloseMinute: Int,
    val startDate: Long,
    val totalScore: Int = 0,
    val lastClosedTotal: Int = 0,
    val lastCloseDate: Long = 0,
    val status: ChallengeStatus = ChallengeStatus.ACTIVE
)
