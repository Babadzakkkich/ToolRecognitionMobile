package com.example.toolrecognition.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ParametersPanel(
    confidence: Float,
    iou: Float,
    onConfidenceChange: (Float) -> Unit,
    onIouChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Настройки",
                    tint = Color(0xFF38B000),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Параметры модели",
                    color = Color(0xFF004B23),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column {
                Text(
                    text = "Уверенность (confidence): ${String.format("%.2f", confidence)}",
                    color = Color(0xFF004B23),
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Slider(
                    value = confidence,
                    onValueChange = onConfidenceChange,
                    valueRange = 0f..1f,
                    steps = 19
                )
                Text(
                    text = "Более высокое значение = меньше ложных срабатываний, но можно пропустить объекты",
                    color = Color(0xFF008000).copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column {
                Text(
                    text = "Пересечение (IoU): ${String.format("%.2f", iou)}",
                    color = Color(0xFF004B23),
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Slider(
                    value = iou,
                    onValueChange = onIouChange,
                    valueRange = 0f..1f,
                    steps = 19
                )
                Text(
                    text = "Порог для подавления дублирующих обнаружений",
                    color = Color(0xFF008000).copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF38B000).copy(alpha = 0.1f)
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Рекомендации:",
                        color = Color(0xFF004B23),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Для четких изображений: confidence = 0.5-0.7",
                        color = Color(0xFF008000),
                        fontSize = 12.sp
                    )
                    Text(
                        text = "• Для сложных условий: confidence = 0.2-0.4",
                        color = Color(0xFF008000),
                        fontSize = 12.sp
                    )
                    Text(
                        text = "• Баланс скорости/точности: IoU = 0.4-0.6",
                        color = Color(0xFF008000),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}