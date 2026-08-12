package com.templesoftware.practicetimestables.ui.navigation

import androidx.annotation.StringRes
import com.templesoftware.practicetimestables.R

enum class NavigationAction { NONE, BACK, HOME }

enum class AppDestination(
    val route: String,
    @param:StringRes val title: Int,
    val navigationAction: NavigationAction,
) {
    HOME("home", R.string.screen_home, NavigationAction.NONE),
    REFRESH("refresh", R.string.screen_refresh, NavigationAction.BACK),
    REPEAT("repeat", R.string.screen_repeat, NavigationAction.BACK),
    QUIZ("quiz", R.string.screen_quiz, NavigationAction.BACK),
    RESULTS("results", R.string.screen_results, NavigationAction.HOME);

    companion object {
        val startDestination: AppDestination = HOME
        val all: List<AppDestination> = entries.toList()

        fun fromRoute(route: String): AppDestination? = entries.firstOrNull { it.route == route }
    }
}
