package com.habitgame.app.ui.navigation

object NavRoutes {
    const val SETUP = "setup"
    const val HOME = "home"
    const val HISTORY = "history"
    const val HISTORY_DETAIL = "history/{logId}"
    const val SETTINGS = "settings"

    fun historyDetail(logId: Int) = "history/$logId"
}
