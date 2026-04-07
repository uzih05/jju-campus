package com.example.galaxy.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

@Serializable sealed interface Route {
    @Serializable data object Dashboard : Route
    @Serializable data object Meal : Route
    @Serializable data object Library : Route
    @Serializable data object Schedule : Route
    @Serializable data object Notices : Route
    @Serializable data object Map : Route
    @Serializable data object Settings : Route
    @Serializable data object Timetable : Route
    @Serializable data object Login : Route
}

enum class BottomNavItem(
    val route: Route,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    Home(Route.Dashboard, "홈", Icons.Filled.Home, Icons.Outlined.Home),
    Timetable(Route.Timetable, "시간표", Icons.Filled.Schedule, Icons.Outlined.Schedule),
    Library(Route.Library, "도서관", Icons.Filled.Book, Icons.Outlined.Book),
    Notices(Route.Notices, "공지", Icons.Filled.Notifications, Icons.Outlined.Notifications),
    Settings(Route.Settings, "설정", Icons.Filled.Settings, Icons.Outlined.Settings),
}
