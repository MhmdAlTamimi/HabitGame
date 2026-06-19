package com.habitgame.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.habitgame.app.data.model.ActionType
import com.habitgame.app.data.model.TodayAction
import kotlinx.coroutines.flow.Flow

@Dao
interface TodayActionDao {
    @Insert
    suspend fun insert(action: TodayAction): Long

    @Update
    suspend fun update(action: TodayAction)

    @Delete
    suspend fun delete(action: TodayAction)

    @Query("SELECT * FROM today_actions WHERE challengeId = :challengeId AND date = :date")
    fun observeActions(challengeId: Int, date: Long): Flow<List<TodayAction>>

    @Query("SELECT * FROM today_actions WHERE challengeId = :challengeId AND date = :date")
    suspend fun getActions(challengeId: Int, date: Long): List<TodayAction>

    @Query("SELECT * FROM today_actions WHERE challengeId = :challengeId AND date = :date AND actionType = :type AND refId = :refId LIMIT 1")
    suspend fun findAction(challengeId: Int, date: Long, type: ActionType, refId: Int): TodayAction?

    @Query("DELETE FROM today_actions WHERE challengeId = :challengeId AND date = :date")
    suspend fun clearDay(challengeId: Int, date: Long)

    @Query("SELECT * FROM today_actions WHERE challengeId = :challengeId AND date = :date AND actionType = :type")
    suspend fun getActionsByType(challengeId: Int, date: Long, type: ActionType): List<TodayAction>
}
