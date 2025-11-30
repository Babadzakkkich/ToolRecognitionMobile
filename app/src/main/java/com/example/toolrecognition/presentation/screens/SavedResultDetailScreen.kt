package com.example.toolrecognition.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.toolrecognition.data.local.SavedAnalysisEntity
import com.example.toolrecognition.data.models.BatchAnalysisResponse
import com.example.toolrecognition.data.models.SingleAnalysisResponse
import com.example.toolrecognition.presentation.components.ResultsDisplay
import com.example.toolrecognition.presentation.viewmodels.MainViewModel
import com.google.gson.Gson

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedResultDetailScreen(
    resultId: Long,
    onNavigateBack: () -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    var entity by remember { mutableStateOf<SavedAnalysisEntity?>(null) }
    val gson = remember { Gson() }

    LaunchedEffect(resultId) {
        entity = viewModel.getSavedById(resultId)
    }

    Column(modifier = Modifier.fillMaxSize()) {

        TopAppBar(
            title = { Text("Детали результата", color = Color.White) },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF38B000))
        )

        if (entity == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF38B000))
            }
            return
        }

        val single: SingleAnalysisResponse? = entity!!.singleAnalysisJson?.let {
            gson.fromJson(it, SingleAnalysisResponse::class.java)
        }

        val batch: BatchAnalysisResponse? = entity!!.batchAnalysisJson?.let {
            gson.fromJson(it, BatchAnalysisResponse::class.java)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = entity!!.name,
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFF004B23)
            )

            entity!!.description?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray
                )
            }

            Divider()

            ResultsDisplay(
                singleResult = single,
                batchResult = batch,
                localSingleImage = entity!!.localAnnotatedImagePath,
                localBatchImages = entity!!.localAnnotatedImagesBatch,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}