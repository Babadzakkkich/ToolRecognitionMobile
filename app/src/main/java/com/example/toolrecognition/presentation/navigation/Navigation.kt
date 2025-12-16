package com.example.toolrecognition.presentation.navigation

sealed class Screen(val route: String) {

    object Home : Screen("home")

    object Parameters : Screen("parameters")

    object Analysis : Screen("analysis")

    object SavedResults : Screen("saved_results")

    object SavedResultDetail : Screen("saved_result_detail/{id}") {
        fun createRoute(id: Long) = "saved_result_detail/$id"
    }
}
