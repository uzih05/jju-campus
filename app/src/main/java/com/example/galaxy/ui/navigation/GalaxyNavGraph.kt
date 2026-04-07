package com.example.galaxy.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.galaxy.ui.screen.dashboard.DashboardScreen
import com.example.galaxy.ui.screen.library.LibraryScreen
import com.example.galaxy.ui.screen.map.MapScreen
import com.example.galaxy.ui.screen.meal.MealScreen
import com.example.galaxy.ui.screen.notices.NoticesScreen
import com.example.galaxy.ui.screen.schedule.ScheduleScreen
import com.example.galaxy.ui.screen.settings.LoginScreen
import com.example.galaxy.ui.screen.settings.SettingsScreen
import com.example.galaxy.ui.screen.timetable.TimetableScreen

@Composable
fun GalaxyNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = Route.Dashboard,
        modifier = modifier,
    ) {
        composable<Route.Dashboard> { DashboardScreen() }
        composable<Route.Meal> { MealScreen() }
        composable<Route.Library> { LibraryScreen() }
        composable<Route.Schedule> { ScheduleScreen() }
        composable<Route.Timetable> { TimetableScreen() }
        composable<Route.Notices> { NoticesScreen() }
        composable<Route.Map> { MapScreen() }
        composable<Route.Settings> { SettingsScreen(navController) }
        composable<Route.Login> {
            LoginScreen(
                onBack = { navController.popBackStack() },
                onLoginSuccess = { navController.popBackStack() },
            )
        }
    }
}
