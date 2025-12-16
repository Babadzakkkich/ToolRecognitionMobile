package com.example.toolrecognition.presentation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.toolrecognition.presentation.screens.*
import com.example.toolrecognition.presentation.viewmodels.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavHost() {
    val navController = rememberNavController()
    val viewModel: MainViewModel = hiltViewModel()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    Scaffold(
        topBar = {
            // Общий TopAppBar для всех экранов
            when (currentRoute) {
                Screen.Home.route -> {
                    TopAppBar(
                        title = {
                            Text(
                                "Распознавание инструментов",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color(0xFF38B000)
                        )
                    )
                }
                Screen.Parameters.route -> {
                    TopAppBar(
                        title = {
                            Text(
                                "Параметры модели",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Назад",
                                    tint = Color.White
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color(0xFF38B000)
                        )
                    )
                }
                Screen.Analysis.route -> {
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                    TopAppBar(
                        title = {
                            Text(
                                "Анализ инструментов",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    "Назад",
                                    tint = Color.White
                                )
                            }
                        },
                        actions = {
                            if (uiState.singleAnalysisResult != null || uiState.batchAnalysisResult != null) {
                                IconButton(onClick = { viewModel.clearResults() }) {
                                    Icon(Icons.Default.Clear, "Очистить", tint = Color.White)
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color(0xFF38B000)
                        )
                    )
                }
                Screen.SavedResults.route -> {
                    TopAppBar(
                        title = {
                            Text(
                                "Сохранённые результаты",
                                color = Color.White
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color(0xFF38B000)
                        )
                    )
                }
                Screen.SavedResultDetail.route -> {
                    TopAppBar(
                        title = { Text("Детали результата", color = Color.White) },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color(0xFF38B000)
                        )
                    )
                }
            }
        },
        bottomBar = { BottomNavigationBar(navController) }
    ) { paddingValues ->
        Surface(
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
}