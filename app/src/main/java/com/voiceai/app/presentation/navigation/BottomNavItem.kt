package com.voiceai.app.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    data object Home : BottomNavItem(
        route = Routes.HOME,
        icon = Icons.Filled.Home,
        label = "Home"
    )

    data object Search : BottomNavItem(
        route = Routes.SEARCH,
        icon = Icons.Filled.Search,
        label = "Search"
    )

    data object Folders : BottomNavItem(
        route = "folders",
        icon = Icons.Filled.Folder,
        label = "Folders"
    )

    data object Settings : BottomNavItem(
        route = Routes.SETTINGS,
        icon = Icons.Filled.Settings,
        label = "Settings"
    )

    companion object {
        val items = listOf(Home, Search, Folders, Settings)
    }
}
