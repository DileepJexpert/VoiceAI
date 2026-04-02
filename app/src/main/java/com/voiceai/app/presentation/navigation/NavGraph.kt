package com.voiceai.app.presentation.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.voiceai.app.presentation.components.SpeedDialFab
import com.voiceai.app.presentation.detail.NoteDetailScreen
import com.voiceai.app.presentation.home.HomeScreen
import com.voiceai.app.presentation.record.RecordScreen
import com.voiceai.app.presentation.scanner.ScanDetailScreen
import com.voiceai.app.presentation.scanner.ScannerScreen
import com.voiceai.app.presentation.search.SearchScreen
import com.voiceai.app.presentation.settings.SettingsScreen

object Routes {
    const val HOME = "home"
    const val RECORD = "record"
    const val NOTE_DETAIL = "note_detail/{noteId}"
    const val SCANNER = "scanner"
    const val SCAN_DETAIL = "scan_detail/{scanId}"
    const val SEARCH = "search"
    const val SETTINGS = "settings"
    const val FOLDERS = "folders"

    fun noteDetail(noteId: Long) = "note_detail/$noteId"
    fun scanDetail(scanId: Long) = "scan_detail/$scanId"
}

private val bottomBarRoutes = setOf(
    Routes.HOME,
    Routes.SEARCH,
    Routes.FOLDERS,
    Routes.SETTINGS
)

@Composable
fun VoiceAINavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.HOME
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in bottomBarRoutes

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                NavigationBar {
                    val leftItems = listOf(BottomNavItem.Home, BottomNavItem.Search)
                    val rightItems = listOf(BottomNavItem.Folders, BottomNavItem.Settings)

                    leftItems.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(Routes.HOME) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }

                    // Spacer item for the centered FAB
                    NavigationBarItem(
                        icon = {},
                        label = {},
                        selected = false,
                        onClick = {},
                        enabled = false
                    )

                    rightItems.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(Routes.HOME) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (showBottomBar) {
                SpeedDialFab(
                    onRecordClick = {
                        navController.navigate(Routes.RECORD)
                    },
                    onScanClick = {
                        navController.navigate(Routes.SCANNER)
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.HOME) {
                HomeScreen(navController = navController)
            }

            composable(Routes.RECORD) {
                RecordScreen(navController = navController)
            }

            composable(
                route = Routes.NOTE_DETAIL,
                arguments = listOf(
                    navArgument("noteId") { type = NavType.LongType }
                )
            ) {
                NoteDetailScreen(navController = navController)
            }

            composable(Routes.SCANNER) {
                ScannerScreen(navController = navController)
            }

            composable(
                route = Routes.SCAN_DETAIL,
                arguments = listOf(
                    navArgument("scanId") { type = NavType.LongType }
                )
            ) {
                ScanDetailScreen(navController = navController)
            }

            composable(Routes.SEARCH) {
                SearchScreen(navController = navController)
            }

            composable(Routes.FOLDERS) {
                FoldersPlaceholderScreen()
            }

            composable(Routes.SETTINGS) {
                SettingsScreen(navController = navController)
            }
        }
    }
}

@Composable
private fun FoldersPlaceholderScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Folders\nComing in Phase 2",
            style = MaterialTheme.typography.titleLarge
        )
    }
}
