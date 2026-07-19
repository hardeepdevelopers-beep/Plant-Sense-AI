package com.plantsense.ai.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.NavKey
import com.plantsense.ai.R

@Composable
fun BottomNavigationBar(
    currentKey: NavKey,
    onNavigate: (NavKey) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        // Home Tab
        NavigationBarItem(
            selected = currentKey == HomeKey,
            onClick = { onNavigate(HomeKey) },
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
        NavigationBarItem(
            selected = currentKey == HistoryKey,
            onClick = { onNavigate(HistoryKey) },
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
        NavigationBarItem(
            selected = currentKey == ProfileKey,
            onClick = { onNavigate(ProfileKey) },
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
