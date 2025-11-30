package com.example.toolrecognition.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.toolrecognition.data.local.SavedAnalysisEntity
import com.example.toolrecognition.presentation.viewmodels.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedResultsScreen(
    onNavigateBack: () -> Unit,
    onOpenResult: (Long) -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val savedResults by viewModel.observeSavedResults().collectAsState(initial = emptyList())

    Column(modifier = Modifier.fillMaxSize()) {

        TopAppBar(
            title = {
                Text(
                    text = "Сохранённые результаты",
                    color = Color.White
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF38B000))
        )

        if (savedResults.isEmpty()) {
            EmptySavedResultsPlaceholder()
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(savedResults) { item ->
                    SavedResultCard(
                        entity = item,
                        onOpen = { onOpenResult(item.id) },
                        onDelete = { viewModel.deleteSaved(item) }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptySavedResultsPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Folder,
            contentDescription = null,
            tint = Color(0xFF38B000),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Нет сохранённых результатов",
            color = Color(0xFF004B23),
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
fun SavedResultCard(
    entity: SavedAnalysisEntity,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() },
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                text = entity.name,
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF004B23)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = entity.description ?: "Без описания",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.History,
                        contentDescription = null,
                        tint = Color(0xFF38B000),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatDate(entity.timestamp),
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                }
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    return java.text.SimpleDateFormat("dd.MM.yyyy HH:mm").format(timestamp)
}