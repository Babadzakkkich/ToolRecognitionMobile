package com.example.toolrecognition.presentation.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Parameters : Screen("parameters")
    object Analysis : Screen("analysis")
}