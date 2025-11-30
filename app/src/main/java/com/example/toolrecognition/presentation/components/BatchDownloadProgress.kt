package com.example.toolrecognition.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.toolrecognition.presentation.viewmodels.BatchDownloadProgress

@Composable
fun BatchDownloadProgressDialog(
    progress: BatchDownloadProgress,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(onDismissRequest = { if (progress.isDownloading) onCancel() }) {
        Card(
            modifier = modifier,
            elevation = CardDefaults.cardElevation(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Иконка загрузки
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "Загрузка",
                    tint = Color(0xFF38B000),
                    modifier = Modifier.size(48.dp)
                )

                // Заголовок
                Text(
                    text = "Сохранение изображений",
                    color = Color(0xFF004B23),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                // Прогресс текст
                Text(
                    text = "Загружено ${progress.downloadedImages} из ${progress.totalImages} изображений",
                    color = Color(0xFF004B23),
                    fontSize = 14.sp
                )

                // Прогресс бар
                LinearProgressIndicator(
                    progress = { progress.currentProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = Color(0xFF38B000),
                    trackColor = Color(0xFF38B000).copy(alpha = 0.2f)
                )

                // Проценты
                Text(
                    text = "${progress.progressPercent}%",
                    color = Color(0xFF008000),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Кнопка отмены
                Button(
                    onClick = onCancel,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF004B23)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Отменить")
                }
            }
        }
    }
}