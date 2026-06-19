package com.habitgame.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.habitgame.app.data.model.Commitment
import kotlinx.coroutines.flow.Flow

@Dao
interface CommitmentDao {
    @Insert
    suspend fun insert(commitment: Commitment): Long

    @Insert
    suspend fun insertAll(commitments: List<Commitment>)

    @Update
    suspend fun update(commitment: Commitment)

    @Query("SELECT * FROM commitments WHERE challengeId = :challengeId AND active = 1")
    fun observeActive(challengeId: Int): Flow<List<Commitment>>

    @Query("SELECT * FROM commitments WHERE challengeId = :challengeId AND active = 1")
    suspend fun getActive(challengeId: Int): List<Commitment>

    @Query("SELECT * FROM commitments WHERE challengeId = :challengeId")
    suspend fun getAll(challengeId: Int): List<Commitment>

    @Query("SELECT * FROM commitments WHERE id = :id")
    suspend fun getById(id: Int): Commitment?
}
