package com.habitgame.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.habitgame.app.data.model.Challenge
import com.habitgame.app.data.model.ChallengeStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface ChallengeDao {
    @Insert
    suspend fun insert(challenge: Challenge): Long

    @Update
    suspend fun update(challenge: Challenge)

    @Query("SELECT * FROM challenges WHERE status = :status LIMIT 1")
    suspend fun getActiveChallenge(status: ChallengeStatus = ChallengeStatus.ACTIVE): Challenge?

    @Query("SELECT * FROM challenges WHERE status = :status LIMIT 1")
    fun observeActiveChallenge(status: ChallengeStatus = ChallengeStatus.ACTIVE): Flow<Challenge?>

    @Query("SELECT * FROM challenges WHERE id = :id")
    suspend fun getById(id: Int): Challenge?
}
