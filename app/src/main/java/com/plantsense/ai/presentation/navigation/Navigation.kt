package com.plantsense.ai.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.plantsense.ai.presentation.camera.CameraScreen
import com.plantsense.ai.presentation.disease.DiseaseScreen
import com.plantsense.ai.presentation.identification.IdentificationScreen
import com.plantsense.ai.presentation.history.HistoryScreen
import com.plantsense.ai.presentation.profile.ProfileScreen
import com.plantsense.ai.presentation.home.HomeScreen

@Composable
fun MainNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = HomeKey
    ) {
        composable<HomeKey> {
            HomeScreen(
                navController = navController,
                onNavigateToCamera = { navController.navigate(CameraKey) },
                onNavigateToHistory = { navController.navigate(HistoryKey) },
                onNavigateToProfile = { navController.navigate(ProfileKey) },
                onNavigateToIdentify = { path, id -> 
                    navController.navigate(IdentificationResultKey(imagePath = path, historyId = id)) 
                },
                onNavigateToDisease = { path, id -> 
                    navController.navigate(DiseaseResultKey(imagePath = path, historyId = id)) 
                }
            )
        }
        composable<CameraKey> {
            CameraScreen(
                onNavigateToHistory = { navController.navigate(HistoryKey) },
                onNavigateToProfile = { navController.navigate(ProfileKey) },
                onNavigateToIdentify = { path -> 
                    navController.navigate(IdentificationResultKey(imagePath = path, historyId = -1)) 
                },
                onNavigateToDisease = { path -> 
                    navController.navigate(DiseaseResultKey(imagePath = path, historyId = -1)) 
                }
            )
        }
        composable<IdentificationResultKey> { backStackEntry ->
            val key: IdentificationResultKey = backStackEntry.toRoute()
            IdentificationScreen(
                imagePath = key.imagePath,
                historyId = key.historyId,
                onBack = { navController.popBackStack() }
            )
        }
        composable<DiseaseResultKey> { backStackEntry ->
            val key: DiseaseResultKey = backStackEntry.toRoute()
            DiseaseScreen(
                imagePath = key.imagePath,
                historyId = key.historyId,
                onBack = { navController.popBackStack() }
            )
        }
        composable<HistoryKey> {
            HistoryScreen(
                navController = navController,
                onNavigateToHome = { navController.navigate(HomeKey) },
                onNavigateToProfile = { navController.navigate(ProfileKey) },
                onNavigateToIdentify = { path, id -> 
                    navController.navigate(IdentificationResultKey(imagePath = path, historyId = id)) 
                },
                onNavigateToDisease = { path, id -> 
                    navController.navigate(DiseaseResultKey(imagePath = path, historyId = id)) 
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable<ProfileKey> {
            ProfileScreen(
                navController = navController,
                onNavigateToHome = { navController.navigate(HomeKey) },
                onNavigateToHistory = { navController.navigate(HistoryKey) },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
