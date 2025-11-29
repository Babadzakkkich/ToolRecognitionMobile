package com.example.toolrecognition.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.toolrecognition.presentation.screens.AnalysisScreen
import com.example.toolrecognition.presentation.screens.HomeScreen
import com.example.toolrecognition.presentation.screens.ParametersScreen
import com.example.toolrecognition.presentation.viewmodels.MainViewModel

@Composable
fun MainNavHost() {
    val navController = rememberNavController()
    val viewModel: MainViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    androidx.compose.material3.Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { paddingValues ->
        Box(
            modifier = androidx.compose.ui.Modifier
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
                        onNavigateToAnalysis = { navController.navigate(Screen.Analysis.route) }
                    )
                }

                composable(Screen.Parameters.route) {
                    ParametersScreen(
                        confidence = uiState.confidence,
                        iou = uiState.iou,
                        onConfidenceChange = { viewModel.updateParameters(it, uiState.iou) },
                        onIouChange = { viewModel.updateParameters(uiState.confidence, it) },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.Analysis.route) {
                    AnalysisScreen(
                        uiState = uiState,
                        onSelectImage = { viewModel.setSelectedImage(it.first, it.second) },
                        onAnalyzeSingleImage = { viewModel.analyzeSingleImage() },
                        onAnalyzeBatchImages = { viewModel.analyzeBatchImages(it.first, it.second) },
                        onClearResults = { viewModel.clearResults() },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}