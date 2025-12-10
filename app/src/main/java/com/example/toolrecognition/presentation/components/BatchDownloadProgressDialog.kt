package com.example.toolrecognition.presentation.components

import androidx.compose.foundation.layout.*
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
    Dialog(onDismissRequest = {}) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Загрузка размеченных изображений",
                    color = Color(0xFF004B23),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                LinearProgressIndicator(
                    progress = { progress.currentProgress },
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF38B000),
                )

                Text(
                    text = "${progress.downloadedImages}/${progress.totalImages} изображений (${progress.progressPercent}%)",
                    color = Color(0xFF008000)
                )

                Spacer(modifier = Modifier.height(8.dp))

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