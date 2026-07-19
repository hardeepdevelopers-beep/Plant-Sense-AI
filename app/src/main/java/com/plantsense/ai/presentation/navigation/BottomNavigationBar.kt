package com.plantsense.ai.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.plantsense.ai.R

@Composable
fun BottomNavigationBar(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        // Home Tab
        val isHomeSelected = currentDestination?.hierarchy?.any { it.hasRoute<HomeKey>() } == true
        NavigationBarItem(
            selected = isHomeSelected,
            onClick = {
                navController.navigate(HomeKey) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = stringResource(R.string.home_cd)
                )
            },
            label = { Text(stringResource(R.string.home_label)) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            )
        )

        // History Tab
        val isHistorySelected = currentDestination?.hierarchy?.any { it.hasRoute<HistoryKey>() } == true
        NavigationBarItem(
            selected = isHistorySelected,
            onClick = {
                navController.navigate(HistoryKey) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = stringResource(R.string.scan_history_cd)
                )
            },
            label = { Text(stringResource(R.string.history_label)) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            )
        )

        // Profile/Settings Tab
        val isProfileSelected = currentDestination?.hierarchy?.any { it.hasRoute<ProfileKey>() } == true
        NavigationBarItem(
            selected = isProfileSelected,
            onClick = {
                navController.navigate(ProfileKey) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = stringResource(R.string.settings_cd)
                )
            },
            label = { Text(stringResource(R.string.profile_label)) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            )
        )
    }
}
