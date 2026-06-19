package com.habitgame.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.habitgame.app.data.dao.*
import com.habitgame.app.data.model.*

@Database(
    entities = [
        Challenge::class,
        Commitment::class,
        Temptation::class,
        DailyLog::class,
        DailyLogEntry::class,
        TodayAction::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun challengeDao(): ChallengeDao
    abstract fun commitmentDao(): CommitmentDao
    abstract fun temptationDao(): TemptationDao
    abstract fun dailyLogDao(): DailyLogDao
    abstract fun dailyLogEntryDao(): DailyLogEntryDao
    abstract fun todayActionDao(): TodayActionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "habit_game_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
