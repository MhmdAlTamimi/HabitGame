package com.habitgame.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.habitgame.app.ui.navigation.AppNavigation
import com.habitgame.app.ui.theme.HabitGameTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HabitGameTheme {
                val navController = rememberNavController()
                AppNavigation(navController = navController)
            }
        }
    }
}
