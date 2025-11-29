package com.example.toolrecognition.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.toolrecognition.R

@Composable
fun BottomNavigationBar(navController: NavController) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == Screen.Home.route,
            onClick = {
                if (currentRoute != Screen.Home.route) {
                    navController.navigate(Screen.Home.route) {
                        // Очищаем бэкстек при переходе на главную
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            },
            icon = {
                Icon(
                    Icons.Default.Home,
                    contentDescription = stringResource(R.string.home)
                )
            },
            label = { Text(stringResource(R.string.home)) }
        )

        NavigationBarItem(
            selected = currentRoute == Screen.Parameters.route,
            onClick = {
                if (currentRoute != Screen.Parameters.route) {
                    navController.navigate(Screen.Parameters.route) {
                        // Сохраняем бэкстек для корректной работы кнопки "Назад"
                        launchSingleTop = true
                    }
                }
            },
            icon = {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = stringResource(R.string.parameters)
                )
            },
            label = { Text(stringResource(R.string.parameters)) }
        )

        NavigationBarItem(
            selected = currentRoute == Screen.Analysis.route,
            onClick = {
                if (currentRoute != Screen.Analysis.route) {
                    navController.navigate(Screen.Analysis.route) {
                        // Сохраняем бэкстек для корректной работы кнопки "Назад"
                        launchSingleTop = true
                    }
                }
            },
            icon = {
                Icon(
                    Icons.Default.Analytics,
                    contentDescription = stringResource(R.string.analysis)
                )
            },
            label = { Text(stringResource(R.string.analysis)) }
        )
    }
}