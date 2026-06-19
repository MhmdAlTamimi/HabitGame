package com.habitgame.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.habitgame.app.data.model.DailyLogEntry

@Dao
interface DailyLogEntryDao {
    @Insert
    suspend fun insert(entry: DailyLogEntry): Long

    @Insert
    suspend fun insertAll(entries: List<DailyLogEntry>)

    @Query("SELECT * FROM daily_log_entries WHERE dailyLogId = :dailyLogId")
    suspend fun getEntriesForLog(dailyLogId: Int): List<DailyLogEntry>
}
