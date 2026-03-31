package com.verihubs.nfcreader.ui.screens

import android.nfc.NfcAdapter
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.verihubs.nfcreader.nfc.NFCViewModel
import com.verihubs.nfcreader.ui.components.BottomNavBar

sealed class Screen(val route: String, val label: String) {
    object Scanner : Screen("scanner", "Scan")
    object History : Screen("history", "Riwayat")
    object Settings : Screen("settings", "Pengaturan")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    nfcAdapter: NfcAdapter?,
    viewModel: NFCViewModel
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            BottomNavBar(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Scanner.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Scanner.route) {
                ScannerScreen(
                    nfcAdapter = nfcAdapter,
                    viewModel = viewModel,
                    onNavigateToHistory = { navController.navigate(Screen.History.route) }
                )
            }
            composable(Screen.History.route) {
                HistoryScreen(viewModel = viewModel)
            }
            composable(Screen.Settings.route) {
                SettingsScreen(viewModel = viewModel)
            }
        }
    }
}
