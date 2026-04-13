package com.scanpang.app.navigation

import androidx.navigation.NavHostController
import androidx.navigation.NavGraph.Companion.findStartDestination

fun NavHostController.navigateToTab(tabGraphRoute: String) {
    navigate(tabGraphRoute) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
