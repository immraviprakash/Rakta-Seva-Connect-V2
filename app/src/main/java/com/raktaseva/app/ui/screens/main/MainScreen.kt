package com.raktaseva.app.ui.screens.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.raktaseva.app.ui.screens.home.HomeScreen
import com.raktaseva.app.ui.screens.donors.DonorsScreen
import com.raktaseva.app.ui.screens.requests.RequestsScreen
import com.raktaseva.app.ui.screens.profile.ProfileScreen
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavScreen(val route: String, val label: String, val icon: ImageVector) {
    object Home : BottomNavScreen("home", "Home", Icons.Default.Home)
    object Donors : BottomNavScreen("donors", "Donors", Icons.Default.Search)
    object Requests : BottomNavScreen("requests", "Requests", Icons.Default.List)
    object Notifications : BottomNavScreen("notifications", "Alerts", Icons.Default.Notifications)
    object Profile : BottomNavScreen("profile", "Profile", Icons.Default.Person)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToRequest: () -> Unit,
    onNavigateToChat: () -> Unit,
    onLogoutClick: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val navController = rememberNavController()
    val items = listOf(
        BottomNavScreen.Home,
        BottomNavScreen.Donors,
        BottomNavScreen.Requests,
        BottomNavScreen.Notifications,
        BottomNavScreen.Profile
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = NavigationBarDefaults.Elevation
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavScreen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavScreen.Home.route) { 
                HomeScreen(onNavigateToRequest = onNavigateToRequest) 
            }
            composable(BottomNavScreen.Donors.route) { DonorsScreen() }
            composable(BottomNavScreen.Requests.route) { RequestsScreen() }
            composable(BottomNavScreen.Notifications.route) { com.raktaseva.app.ui.screens.notifications.NotificationsScreen() }
            composable(BottomNavScreen.Profile.route) { 
                ProfileScreen(
                    onChatClick = onNavigateToChat,
                    onLogoutClick = onLogoutClick,
                    onEditProfileClick = onNavigateToEditProfile,
                    onSettingsClick = onNavigateToSettings
                ) 
            }
        }
    }
}
