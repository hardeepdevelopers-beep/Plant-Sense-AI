package com.plantsense.ai.presentation.navigation

import kotlinx.serialization.Serializable

sealed interface AppRoute {
    @Serializable data object Home : AppRoute
    @Serializable data object Camera : AppRoute
    @Serializable data class Identify(val path: String, val historyId: Int = -1) : AppRoute
    @Serializable data class Disease(val path: String, val historyId: Int = -1) : AppRoute
    @Serializable data object History : AppRoute
    @Serializable data object Profile : AppRoute
}
