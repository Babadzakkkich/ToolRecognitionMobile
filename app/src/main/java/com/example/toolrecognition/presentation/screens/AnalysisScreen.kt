package com.example.toolrecognition.presentation.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.toolrecognition.presentation.components.FileUploadComponent
import com.example.toolrecognition.presentation.components.LoadingSpinner
import com.example.toolrecognition.presentation.components.ResultsDisplay
import com.example.toolrecognition.presentation.viewmodels.MainUiState
import kotlinx.coroutines.launch
import java.io.InputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisScreen(
    uiState: MainUiState,
    onSelectImage: (Pair<Uri?, Bitmap?>) -> Unit,
    onAnalyzeSingleImage: () -> Unit,
    onAnalyzeBatchImages: (Pair<InputStream, String>) -> Unit,
    onClearResults: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var showFileTypeDialog by remember { mutableStateOf(false) }

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
                        message = "Ошибка при загрузке изображения: ${e.message ?: "Неизвестная ошибка"}",
                        withDismissAction = true,
                        duration = SnackbarDuration.Long
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
                            message = "Не удалось прочитать файл",
                            withDismissAction = true,
                            duration = SnackbarDuration.Long
                        )
                    }
                }
            } catch (e: Exception) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Ошибка при загрузке архива: ${e.message ?: "Неизвестная ошибка"}",
                        withDismissAction = true,
                        duration = SnackbarDuration.Long
                    )
                }
            }
        }
    }

    // Обработка ошибок
    LaunchedEffect(key1 = uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                withDismissAction = true,
                duration = SnackbarDuration.Long
            )
        }
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // TopAppBar без Scaffold
        TopAppBar(
            title = {
                Text(
                    text = "Анализ инструментов",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF38B000)
            ),
            actions = {
                if (uiState.singleAnalysisResult != null || uiState.batchAnalysisResult != null) {
                    IconButton(
                        onClick = onClearResults,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Очистить результаты",
                            tint = Color.White
                        )
                    }
                }
            }
        )

        // Snackbar Host
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Контент
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
                    // Информационная карточка
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF38B000).copy(alpha = 0.1f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Analytics,
                                    contentDescription = "Анализ",
                                    tint = Color(0xFF38B000),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Анализ инструментов",
                                    color = Color(0xFF004B23),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Загрузите изображение или ZIP-архив для анализа инструментов. Приложение определит наличие всех необходимых инструментов и покажет результаты обнаружения.",
                                color = Color(0xFF004B23).copy(alpha = 0.8f),
                                fontSize = 14.sp
                            )
                        }
                    }

                    // Компонент загрузки файлов
                    FileUploadComponent(
                        selectedFileName = uiState.selectedImageUri?.let {
                            getFileName(context, it)
                        },
                        onSelectImage = { imagePicker.launch("image/*") },
                        onSelectZip = { zipPicker.launch("application/zip") },
                        onSelectFile = { showFileTypeDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Предпросмотр изображения (только если есть выбранное изображение)
                    if (uiState.selectedImageBitmap != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Предпросмотр изображения:",
                                        color = Color(0xFF004B23),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text(
                                        text = uiState.selectedImageUri?.let { getFileName(context, it) } ?: "image.jpg",
                                        color = Color(0xFF008000),
                                        fontSize = 12.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .align(Alignment.CenterHorizontally),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        bitmap = uiState.selectedImageBitmap.asImageBitmap(),
                                        contentDescription = "Выбранное изображение",
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }

                        // Кнопка анализа для одиночного изображения
                        Button(
                            onClick = onAnalyzeSingleImage,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF38B000)
                            ),
                            enabled = !uiState.isLoading
                        ) {
                            Text(
                                text = "Начать анализ изображения",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Отображение результатов
                    if (uiState.singleAnalysisResult != null || uiState.batchAnalysisResult != null) {
                        Text(
                            text = "Результаты анализа:",
                            color = Color(0xFF004B23),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth()
                        )

                        when {
                            uiState.singleAnalysisResult != null -> {
                                ResultsDisplay(
                                    singleResult = uiState.singleAnalysisResult,
                                    batchResult = null,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            uiState.batchAnalysisResult != null -> {
                                ResultsDisplay(
                                    singleResult = null,
                                    batchResult = uiState.batchAnalysisResult,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        // Кнопка для нового анализа
                        Button(
                            onClick = onClearResults,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF008000)
                            )
                        ) {
                            Text(
                                text = "Новый анализ",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else if (uiState.selectedImageBitmap == null && uiState.batchAnalysisResult == null) {
                        // Сообщение, когда ничего не выбрано
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFF5F5F5)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Analytics,
                                    contentDescription = "Анализ",
                                    tint = Color(0xFF38B000),
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    text = "Выберите файл для анализа",
                                    color = Color(0xFF004B23),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Text(
                                    text = "Загрузите изображение или ZIP-архив с инструментами, чтобы начать анализ",
                                    color = Color(0xFF008000).copy(alpha = 0.7f),
                                    fontSize = 14.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            // Snackbar размещаем в Box
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }

    // Диалог выбора типа файла
    if (showFileTypeDialog) {
        AlertDialog(
            onDismissRequest = { showFileTypeDialog = false },
            title = {
                Text(
                    text = "Выберите тип файла",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF004B23)
                )
            },
            text = {
                Text(
                    text = "Что вы хотите загрузить для анализа?",
                    color = Color(0xFF004B23)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        imagePicker.launch("image/*")
                        showFileTypeDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF38B000)
                    )
                ) {
                    Text("Изображение")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        zipPicker.launch("application/zip")
                        showFileTypeDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF008000)
                    )
                ) {
                    Text("ZIP архив")
                }
            }
        )
    }
}

// Вспомогательная функция для получения имени файла
private fun getFileName(context: Context, uri: Uri): String? {
    return try {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex("_display_name")
            if (nameIndex != -1) {
                cursor.moveToFirst()
                cursor.getString(nameIndex)
            } else {
                uri.lastPathSegment ?: "unknown_file"
            }
        } ?: uri.lastPathSegment ?: "unknown_file"
    } catch (e: Exception) {
        uri.lastPathSegment ?: "unknown_file"
    }
}