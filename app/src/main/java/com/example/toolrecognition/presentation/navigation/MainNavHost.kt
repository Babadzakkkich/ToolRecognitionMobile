package com.example.toolrecognition.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
    val viewModel: MainViewModel = hiltViewModel()

    Box(
        modifier = Modifier.fillMaxSize()
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
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                ParametersScreen(
                    confidence = uiState.confidence,
                    iou = uiState.iou,
                    onConfidenceChange = { viewModel.updateParameters(it, uiState.iou) },
                    onIouChange = { viewModel.updateParameters(uiState.confidence, it) },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Analysis.route) {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val batchProgress by viewModel.batchDownloadProgress.collectAsStateWithLifecycle()

                AnalysisScreen(
                    uiState = uiState,
                    batchProgress = batchProgress,
                    onSelectImage = { viewModel.setSelectedImage(it.first, it.second) },
                    onAnalyzeSingleImage = { viewModel.analyzeSingleImage() },
                    onAnalyzeBatchImages = { viewModel.analyzeBatchImages(it.first, it.second) },
                    onClearResults = { viewModel.clearResults() },
                    onNavigateBack = { navController.popBackStack() },
                    onSaveResult = { name, description ->
                        viewModel.saveCurrentResult(name, description)
                    },
                    onCancelBatchDownload = { viewModel.cancelBatchDownload() }
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