package com.habitgame.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.habitgame.app.data.model.DailyLog
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyLogDao {
    @Insert
    suspend fun insert(dailyLog: DailyLog): Long

    @Update
    suspend fun update(dailyLog: DailyLog)

    @Query("SELECT * FROM daily_logs WHERE challengeId = :challengeId AND closed = 0 LIMIT 1")
    suspend fun getOpenLog(challengeId: Int): DailyLog?

    @Query("SELECT * FROM daily_logs WHERE challengeId = :challengeId AND date = :date LIMIT 1")
    suspend fun getByDate(challengeId: Int, date: Long): DailyLog?

    @Query("SELECT * FROM daily_logs WHERE challengeId = :challengeId AND closed = 1 ORDER BY date DESC")
    fun observeClosedLogs(challengeId: Int): Flow<List<DailyLog>>

    @Query("SELECT * FROM daily_logs WHERE challengeId = :challengeId AND closed = 1 ORDER BY date DESC")
    suspend fun getClosedLogs(challengeId: Int): List<DailyLog>
}
