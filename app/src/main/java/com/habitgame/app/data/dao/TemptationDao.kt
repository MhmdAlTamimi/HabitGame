package com.habitgame.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.habitgame.app.data.model.Temptation
import kotlinx.coroutines.flow.Flow

@Dao
interface TemptationDao {
    @Insert
    suspend fun insert(temptation: Temptation): Long

    @Insert
    suspend fun insertAll(temptations: List<Temptation>)

    @Update
    suspend fun update(temptation: Temptation)

    @Query("SELECT * FROM temptations WHERE challengeId = :challengeId AND active = 1")
    fun observeActive(challengeId: Int): Flow<List<Temptation>>

    @Query("SELECT * FROM temptations WHERE challengeId = :challengeId AND active = 1")
    suspend fun getActive(challengeId: Int): List<Temptation>

    @Query("SELECT * FROM temptations WHERE challengeId = :challengeId")
    suspend fun getAll(challengeId: Int): List<Temptation>

    @Query("SELECT * FROM temptations WHERE id = :id")
    suspend fun getById(id: Int): Temptation?
}
