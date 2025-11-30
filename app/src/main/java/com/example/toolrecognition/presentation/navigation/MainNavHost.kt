package com.example.toolrecognition.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.toolrecognition.presentation.screens.*
import com.example.toolrecognition.presentation.viewmodels.MainViewModel

@Composable
fun MainNavHost() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route
            ) {
                composable(Screen.Home.route) {
                    HomeScreen(
                        onNavigateToParameters = { navController.navigate(Screen.Parameters.route) },
                        onNavigateToAnalysis = { navController.navigate(Screen.Analysis.route) },
                        onNavigateToSavedResults = { navController.navigate(Screen.SavedResults.route) }
                    )
                }

                composable(Screen.Parameters.route) {
                    ParametersScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.Analysis.route) {
                    // Получаем viewModel внутри AnalysisScreen
                    val viewModel: MainViewModel = hiltViewModel()
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                    AnalysisScreen(
                        uiState = uiState,
                        onSelectImage = { viewModel.setSelectedImage(it.first, it.second) },
                        onAnalyzeSingleImage = { viewModel.analyzeSingleImage() },
                        onAnalyzeBatchImages = { viewModel.analyzeBatchImages(it.first, it.second) },
                        onClearResults = { viewModel.clearResults() },
                        onNavigateBack = { navController.popBackStack() },
                        onSaveResult = { name, description ->
                            viewModel.saveCurrentResult(name, description)
                        }
                    )
                }

                composable(Screen.SavedResults.route) {
                    SavedResultsScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onOpenResult = { id ->
                            navController.navigate(Screen.SavedResultDetail.createRoute(id))
                        }
                    )
                }

                composable(
                    route = Screen.SavedResultDetail.route,
                    arguments = listOf(navArgument("id") { type = NavType.LongType })
                ) { backStackEntry ->
                    val id = backStackEntry.arguments?.getLong("id") ?: 0L

                    SavedResultDetailScreen(
                        resultId = id,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}