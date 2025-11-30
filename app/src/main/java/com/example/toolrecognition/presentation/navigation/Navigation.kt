package com.example.toolrecognition.presentation.navigation

sealed class Screen(val route: String) {

    object Home : Screen("home")

    object Parameters : Screen("parameters")

    object Analysis : Screen("analysis")

    // Новый экран: список сохранённых результатов
    object SavedResults : Screen("saved_results")

    // Новый экран: подробный просмотр сохранённого результата
    object SavedResultDetail : Screen("saved_result_detail/{id}") {
        fun createRoute(id: Long) = "saved_result_detail/$id"
    }
}
