package com.example.toolrecognition.presentation.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.toolrecognition.presentation.components.BatchDownloadProgressDialog
import com.example.toolrecognition.presentation.components.FileUploadComponent
import com.example.toolrecognition.presentation.components.LoadingSpinner
import com.example.toolrecognition.presentation.components.ResultsDisplay
import com.example.toolrecognition.presentation.viewmodels.BatchDownloadProgress
import com.example.toolrecognition.presentation.viewmodels.MainUiState
import kotlinx.coroutines.launch
import java.io.InputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisScreen(
    uiState: MainUiState,
    batchProgress: BatchDownloadProgress,
    onSelectImage: (Pair<Uri?, Bitmap?>) -> Unit,
    onAnalyzeSingleImage: () -> Unit,
    onAnalyzeBatchImages: (Pair<InputStream, String>) -> Unit,
    onClearResults: () -> Unit,
    onNavigateBack: () -> Unit,
    onSaveResult: (String, String?) -> Unit,
    onCancelBatchDownload: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var showFileTypeDialog by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }

    var saveName by remember { mutableStateOf("") }
    var saveDescription by remember { mutableStateOf("") }

    // ------- IMAGE PICKER -------
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                onSelectImage(Pair(uri, bitmap))
            } catch (e: Exception) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        "Ошибка при загрузке изображения: ${e.message ?: "Неизвестная ошибка"}",
                        withDismissAction = true
                    )
                }
            }
        }
    }

    val zipPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val fileName = getFileName(context, uri)
                if (inputStream != null && fileName != null) {
                    onAnalyzeBatchImages(Pair(inputStream, fileName))
                } else {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            "Не удалось прочитать файл",
                            withDismissAction = true
                        )
                    }
                }
            } catch (e: Exception) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        "Ошибка при загрузке архива: ${e.message}",
                        withDismissAction = true
                    )
                }
            }
        }
    }

    // -------- ERROR HANDLER --------
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error, withDismissAction = true)
        }
    }

    // -------------------------------- UI --------------------------------

    Column(modifier = modifier.fillMaxSize()) {

        // ----- TOP BAR -----
        TopAppBar(
            title = {
                Text("Анализ инструментов", color = Color.White, fontWeight = FontWeight.Bold)
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад", tint = Color.White)
                }
            },
            actions = {
                if (uiState.singleAnalysisResult != null || uiState.batchAnalysisResult != null) {
                    IconButton(onClick = onClearResults) {
                        Icon(Icons.Default.Clear, "Очистить", tint = Color.White)
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF38B000))
        )

        // ----- MAIN CONTENT -----
        Box(modifier = Modifier.fillMaxSize()) {

            if (uiState.isLoading) {
                LoadingSpinner()
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    // ----- Info Card -----
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF38B000).copy(alpha = 0.1f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Analytics,
                                    contentDescription = null,
                                    tint = Color(0xFF38B000),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Анализ инструментов",
                                    color = Color(0xFF004B23),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Загрузите изображение или ZIP-архив для анализа.",
                                color = Color(0xFF004B23).copy(alpha = 0.8f)
                            )
                        }
                    }

                    // ----- File Upload -----
                    FileUploadComponent(
                        selectedFileName = uiState.selectedImageUri?.let { getFileName(context, it) },
                        onSelectImage = { imagePicker.launch("image/*") },
                        onSelectZip = { zipPicker.launch("application/zip") },
                        onSelectFile = { showFileTypeDialog = true }
                    )

                    // ----- IMAGE PREVIEW -----
                    uiState.selectedImageBitmap?.let { bitmap ->
                        ImagePreviewCard(bitmap, uiState.selectedImageUri, context)
                        AnalyzeButton(uiState.isLoading, onAnalyzeSingleImage)
                    }

                    // ----- RESULTS -----
                    if (uiState.singleAnalysisResult != null || uiState.batchAnalysisResult != null) {
                        Text(
                            "Результаты анализа:",
                            color = Color(0xFF004B23),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        ResultsDisplay(
                            singleResult = uiState.singleAnalysisResult,
                            batchResult = uiState.batchAnalysisResult
                        )

                        // ----- SAVE RESULT BUTTON -----
                        Button(
                            onClick = { showSaveDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004B23))
                        ) {
                            Text("Сохранить результат", fontSize = 16.sp)
                        }

                        // ----- NEW ANALYSIS -----
                        Button(
                            onClick = onClearResults,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF008000))
                        ) {
                            Text("Новый анализ", fontSize = 16.sp)
                        }
                    } else {
                        InitialPlaceholder()
                    }
                }
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }

    // -------- BATCH DOWNLOAD PROGRESS DIALOG --------
    if (batchProgress.isDownloading) {
        BatchDownloadProgressDialog(
            progress = batchProgress,
            onCancel = onCancelBatchDownload
        )
    }

    // -------- FILE TYPE DIALOG --------
    if (showFileTypeDialog) {
        FileTypeDialog(
            onDismiss = { showFileTypeDialog = false },
            onSelectImage = { imagePicker.launch("image/*") },
            onSelectZip = { zipPicker.launch("application/zip") }
        )
    }

    // -------- SAVE RESULT DIALOG --------
    if (showSaveDialog) {
        SaveResultDialog(
            name = saveName,
            description = saveDescription,
            onNameChange = { saveName = it },
            onDescriptionChange = { saveDescription = it },
            onDismiss = { showSaveDialog = false },
            onSave = {
                if (saveName.isNotBlank()) {
                    onSaveResult(saveName, saveDescription.ifBlank { null })
                    showSaveDialog = false
                }
            }
        )
    }
}

// ---------------------------------------------------------------------
// HELPERS
// ---------------------------------------------------------------------

@Composable
private fun ImagePreviewCard(bitmap: Bitmap, uri: Uri?, context: Context) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Предпросмотр:",
                    color = Color(0xFF004B23),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    getFileName(context, uri!!) ?: "image.jpg",
                    color = Color(0xFF008000),
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun AnalyzeButton(isLoading: Boolean, onAnalyze: () -> Unit) {
    Button(
        onClick = onAnalyze,
        modifier = Modifier.fillMaxWidth(),
        enabled = !isLoading,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38B000))
    ) {
        Text("Начать анализ", fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun InitialPlaceholder() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.Analytics,
                contentDescription = null,
                tint = Color(0xFF38B000),
                modifier = Modifier.size(48.dp)
            )
            Text("Выберите файл для анализа", color = Color(0xFF004B23), fontSize = 18.sp)
        }
    }
}

@Composable
private fun FileTypeDialog(
    onDismiss: () -> Unit,
    onSelectImage: () -> Unit,
    onSelectZip: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Выберите тип файла", fontWeight = FontWeight.Bold) },
        text = { Text("Что вы хотите загрузить?") },
        confirmButton = {
            Button(onClick = {
                onSelectImage()
                onDismiss()
            }) {
                Text("Изображение")
            }
        },
        dismissButton = {
            Button(onClick = {
                onSelectZip()
                onDismiss()
            }) {
                Text("ZIP архив")
            }
        }
    )
}

@Composable
private fun SaveResultDialog(
    name: String,
    description: String,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Сохранение результата") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("Название") }
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = onDescriptionChange,
                    label = { Text("Описание (необязательно)") }
                )
            }
        },
        confirmButton = {
            Button(onClick = onSave) { Text("Сохранить") }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text("Отмена") }
        }
    )
}

// Utility
private fun getFileName(context: Context, uri: Uri): String? {
    return try {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex("_display_name")
            if (index != -1) {
                cursor.moveToFirst()
                cursor.getString(index)
            } else uri.lastPathSegment
        } ?: uri.lastPathSegment
    } catch (e: Exception) {
        uri.lastPathSegment
    }
}