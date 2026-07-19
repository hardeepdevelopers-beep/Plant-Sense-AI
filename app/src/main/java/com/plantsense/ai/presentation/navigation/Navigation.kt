package com.plantsense.ai.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.plantsense.ai.presentation.camera.CameraScreen
import com.plantsense.ai.presentation.disease.DiseaseScreen
import com.plantsense.ai.presentation.identification.IdentificationScreen
import com.plantsense.ai.presentation.history.HistoryScreen
import com.plantsense.ai.presentation.profile.ProfileScreen
import com.plantsense.ai.presentation.home.HomeScreen

@Composable
fun MainNavigation() {
    val backStack = rememberNavBackStack(HomeKey)

    fun navigateTo(targetKey: NavKey) {
        if (backStack.lastOrNull() == targetKey) return
        backStack.clear()
        backStack.add(HomeKey)
        if (targetKey != HomeKey) {
            backStack.add(targetKey)
        }
    }

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<HomeKey> {
                HomeScreen(
                    onNavigateToCamera = { backStack.add(CameraKey) },
                    onNavigateToHistory = { navigateTo(HistoryKey) },
                    onNavigateToProfile = { navigateTo(ProfileKey) },
                    onNavigateToIdentify = { path -> backStack.add(IdentificationResultKey(path)) },
                    onNavigateToDisease = { path -> backStack.add(DiseaseResultKey(path)) }
                )
            }
            entry<CameraKey> {
                CameraScreen(
                    onNavigateToHistory = { navigateTo(HistoryKey) },
                    onNavigateToProfile = { navigateTo(ProfileKey) },
                    onNavigateToIdentify = { path -> backStack.add(IdentificationResultKey(path)) },
                    onNavigateToDisease = { path -> backStack.add(DiseaseResultKey(path)) }
                )
            }
            entry<IdentificationResultKey> { key ->
                IdentificationScreen(
                    imagePath = key.imagePath,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<DiseaseResultKey> { key ->
                DiseaseScreen(
                    imagePath = key.imagePath,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<HistoryKey> {
                HistoryScreen(
                    onNavigateToHome = { navigateTo(HomeKey) },
                    onNavigateToProfile = { navigateTo(ProfileKey) },
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<ProfileKey> {
                ProfileScreen(
                    onNavigateToHome = { navigateTo(HomeKey) },
                    onNavigateToHistory = { navigateTo(HistoryKey) },
                    onBack = { backStack.removeLastOrNull() }
                )
            }
        }
    )
}
