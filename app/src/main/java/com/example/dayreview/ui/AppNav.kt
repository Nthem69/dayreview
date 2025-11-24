package com.example.dayreview.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.feature.today.TodayScreen

sealed class Route(val path: String) { object Today : Route("today") }

@Composable
fun AppNav() {
    val nav = rememberNavController()
    NavHost(nav, startDestination = Route.Today.path) {
        composable(Route.Today.path) { TodayScreen() }
    }
}
