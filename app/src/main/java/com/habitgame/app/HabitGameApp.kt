package com.habitgame.app

import android.app.Application
import com.habitgame.app.data.database.AppDatabase

class HabitGameApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
}
