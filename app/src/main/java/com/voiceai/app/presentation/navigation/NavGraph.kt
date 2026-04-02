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
import com.voiceai.app.presentation.actionitems.ActionItemsScreen
import com.voiceai.app.presentation.businesscard.BusinessCardDetailScreen
import com.voiceai.app.presentation.chat.AIChatScreen
import com.voiceai.app.presentation.components.SpeedDialFab
import com.voiceai.app.presentation.detail.NoteDetailScreen
import com.voiceai.app.presentation.digest.DailyDigestScreen
import com.voiceai.app.presentation.expenses.ExpenseTrackerScreen
import com.voiceai.app.presentation.folders.FoldersScreen
import com.voiceai.app.presentation.home.HomeScreen
import com.voiceai.app.presentation.onboarding.OnboardingScreen
import com.voiceai.app.presentation.qrscanner.QRScannerScreen
import com.voiceai.app.presentation.receipt.ReceiptDetailScreen
import com.voiceai.app.presentation.record.RecordScreen
import com.voiceai.app.presentation.scanner.ScanDetailScreen
import com.voiceai.app.presentation.scanner.ScannerScreen
import com.voiceai.app.presentation.search.SearchScreen
import com.voiceai.app.presentation.settings.SettingsScreen
import com.voiceai.app.presentation.splash.SplashScreen

object Routes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val RECORD = "record"
    const val NOTE_DETAIL = "note_detail/{noteId}"
    const val SCANNER = "scanner"
    const val SCANNER_BUSINESS_CARD = "scanner_business_card"
    const val SCANNER_RECEIPT = "scanner_receipt"
    const val QR_SCANNER = "qr_scanner"
    const val SCAN_DETAIL = "scan_detail/{scanId}"
    const val BUSINESS_CARD_DETAIL = "business_card_detail/{contactId}"
    const val RECEIPT_DETAIL = "receipt_detail/{expenseId}"
    const val SEARCH = "search"
    const val SETTINGS = "settings"
    const val FOLDERS = "folders"
    const val AI_CHAT = "ai_chat"
    const val ACTION_ITEMS = "action_items"
    const val EXPENSE_TRACKER = "expense_tracker"
    const val DAILY_DIGEST = "daily_digest"

    fun noteDetail(noteId: Long) = "note_detail/$noteId"
    fun scanDetail(scanId: Long) = "scan_detail/$scanId"
    fun businessCardDetail(contactId: Long) = "business_card_detail/$contactId"
    fun receiptDetail(expenseId: Long) = "receipt_detail/$expenseId"
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
    startDestination: String = Routes.SPLASH
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
                    onRecordClick = { navController.navigate(Routes.RECORD) },
                    onScanClick = { navController.navigate(Routes.SCANNER) },
                    onBusinessCardClick = { navController.navigate(Routes.SCANNER_BUSINESS_CARD) },
                    onReceiptClick = { navController.navigate(Routes.SCANNER_RECEIPT) },
                    onQRClick = { navController.navigate(Routes.QR_SCANNER) }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Splash & Onboarding
            composable(Routes.SPLASH) {
                SplashScreen(
                    onNavigateToHome = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    },
                    onNavigateToOnboarding = {
                        navController.navigate(Routes.ONBOARDING) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.ONBOARDING) {
                OnboardingScreen(
                    onComplete = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                        }
                    }
                )
            }

            // Main tabs
            composable(Routes.HOME) {
                HomeScreen(navController = navController)
            }

            composable(Routes.SEARCH) {
                SearchScreen(navController = navController)
            }

            composable(Routes.FOLDERS) {
                FoldersScreen(navController = navController)
            }

            composable(Routes.SETTINGS) {
                SettingsScreen(navController = navController)
            }

            // Voice Notes
            composable(Routes.RECORD) {
                RecordScreen(navController = navController)
            }

            composable(
                route = Routes.NOTE_DETAIL,
                arguments = listOf(navArgument("noteId") { type = NavType.LongType })
            ) {
                NoteDetailScreen(navController = navController)
            }

            // Document Scanner
            composable(Routes.SCANNER) {
                ScannerScreen(navController = navController)
            }

            composable(Routes.SCANNER_BUSINESS_CARD) {
                ScannerScreen(navController = navController)
            }

            composable(Routes.SCANNER_RECEIPT) {
                ScannerScreen(navController = navController)
            }

            composable(
                route = Routes.SCAN_DETAIL,
                arguments = listOf(navArgument("scanId") { type = NavType.LongType })
            ) {
                ScanDetailScreen(navController = navController)
            }

            // QR Scanner
            composable(Routes.QR_SCANNER) {
                QRScannerScreen(navController = navController)
            }

            // Business Card Detail
            composable(
                route = Routes.BUSINESS_CARD_DETAIL,
                arguments = listOf(navArgument("contactId") { type = NavType.LongType })
            ) {
                BusinessCardDetailScreen(navController = navController)
            }

            // Receipt Detail
            composable(
                route = Routes.RECEIPT_DETAIL,
                arguments = listOf(navArgument("expenseId") { type = NavType.LongType })
            ) {
                ReceiptDetailScreen(navController = navController)
            }

            // AI Chat
            composable(Routes.AI_CHAT) {
                AIChatScreen(navController = navController)
            }

            // Action Items
            composable(Routes.ACTION_ITEMS) {
                ActionItemsScreen(navController = navController)
            }

            // Expense Tracker
            composable(Routes.EXPENSE_TRACKER) {
                ExpenseTrackerScreen(navController = navController)
            }

            // Daily Digest
            composable(Routes.DAILY_DIGEST) {
                DailyDigestScreen(navController = navController)
            }
        }
    }
}
