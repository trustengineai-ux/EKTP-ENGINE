package com.verihubs.nfcreader.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.verihubs.nfcreader.ui.screens.Screen
import com.verihubs.nfcreader.ui.theme.*

data class NavItem(
    val screen: Screen,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val navItems = listOf(
    NavItem(Screen.Scanner, Icons.Filled.Nfc, Icons.Outlined.Nfc),
    NavItem(Screen.History, Icons.Filled.History, Icons.Outlined.History),
    NavItem(Screen.Settings, Icons.Filled.Settings, Icons.Outlined.Settings)
)

@Composable
fun BottomNavBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = SurfaceDark,
        contentColor = TextPrimary,
        tonalElevation = 0.dp
    ) {
        navItems.forEach { item ->
            val selected = currentRoute == item.screen.route
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(item.screen.route) },
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.screen.label
                    )
                },
                label = {
                    Text(item.screen.label, style = MaterialTheme.typography.labelSmall)
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryBlue,
                    selectedTextColor = PrimaryBlue,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary,
                    indicatorColor = PrimaryBlue.copy(alpha = 0.12f)
                )
            )
        }
    }
}
