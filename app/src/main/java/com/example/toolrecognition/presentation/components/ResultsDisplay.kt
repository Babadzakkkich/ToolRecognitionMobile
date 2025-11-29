package com.example.toolrecognition.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.toolrecognition.data.models.BatchAnalysisResponse
import com.example.toolrecognition.data.models.SingleAnalysisResponse
import com.example.toolrecognition.utils.ImageUrlBuilder

@Composable
fun ResultsDisplay(
    singleResult: SingleAnalysisResponse?,
    batchResult: BatchAnalysisResponse?,
    modifier: Modifier = Modifier
) {
    var currentImageIndex by remember { mutableStateOf(0) }

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
                    imageVector = Icons.Default.Analytics,
                    contentDescription = "Анализ",
                    tint = Color(0xFF38B000),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Анализ полноты набора инструментов",
                    color = Color(0xFF004B23),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (batchResult != null && batchResult.results.isNotEmpty()) {
                // Для batch результатов показываем только слайдер
                ImageSlider(
                    results = batchResult.results,
                    currentIndex = currentImageIndex,
                    onIndexChange = { currentImageIndex = it },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Показываем только результаты анализа БЕЗ дублирования изображения
                val currentResult = batchResult.results[currentImageIndex]
                SingleResultView(currentResult.analysisResult)

                Spacer(modifier = Modifier.height(16.dp))

                // Сводка по batch
                BatchSummaryView(batchResult)

            } else if (singleResult != null) {
                // Для одиночного результата показываем изображение и результаты
                SingleResultWithImage(
                    analysis = singleResult.analysisResult,
                    annotatedImagePath = singleResult.config?.annotatedImagePath
                )
            }
        }
    }
}

@Composable
private fun SingleResultWithImage(
    analysis: com.example.toolrecognition.data.models.AnalysisResult,
    annotatedImagePath: String?
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Показываем размеченное изображение только для одиночного результата
        if (annotatedImagePath != null) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Размеченное изображение:",
                        color = Color(0xFF004B23),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Используем ImageUrlBuilder для построения URL
                    val imageUrl = ImageUrlBuilder.buildAnnotatedImageUrl(annotatedImagePath)
                    ImageWithLoader(
                        imageUrl = imageUrl,
                        contentDescription = "Размеченное изображение",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                    )
                }
            }
        } else {
            // Если изображения нет, показываем информационное сообщение
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF5F5F5)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Изображение не содержит обнаруженных объектов",
                        color = Color(0xFF004B23),
                        fontWeight = FontWeight.Medium,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Модель не нашла инструментов на этом изображении",
                        color = Color(0xFF008000).copy(alpha = 0.7f),
                        fontSize = 14.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }

        // Остальной контент (статус, инструменты и т.д.)
        SingleResultView(analysis)
    }
}

@Composable
private fun SingleResultView(analysis: com.example.toolrecognition.data.models.AnalysisResult) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Статус
        Card(
            colors = CardDefaults.cardColors(
                containerColor = getStatusColor(analysis.status)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = analysis.message,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Обнаружено: ${analysis.totalDetections} из ${analysis.expectedCount} инструментов",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp
                )
            }
        }

        // Отсутствующие инструменты
        if (analysis.missingTools.isNotEmpty()) {
            ToolsSection(
                title = "Отсутствующие инструменты",
                tools = analysis.missingTools,
                icon = Icons.Default.Error,
                color = Color(0xFF38B000)
            )
        }

        // Лишние инструменты
        if (analysis.extraTools.isNotEmpty()) {
            ToolsSection(
                title = "Лишние инструменты",
                tools = analysis.extraTools,
                icon = Icons.Default.Warning,
                color = Color(0xFF008000)
            )
        }

        // Обнаруженные инструменты
        if (analysis.detections.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Обнаружено",
                            tint = Color(0xFF38B000)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Обнаруженные инструменты",
                            color = Color(0xFF004B23),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        analysis.detections.forEach { detection ->
                            DetectionItemView(detection)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BatchSummaryView(result: BatchAnalysisResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF38B000).copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Сводка по всем изображениям:",
                color = Color(0xFF004B23),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SummaryItem(
                        count = result.summary["complete"] ?: 0,
                        label = "Полные наборы",
                        color = Color(0xFF008000)
                    )
                    SummaryItem(
                        count = result.summary["missing"] ?: 0,
                        label = "Неполные наборы",
                        color = Color(0xFF38B000)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SummaryItem(
                        count = (result.summary["duplicates"] ?: 0) +
                                (result.summary["duplicates_only"] ?: 0) +
                                (result.summary["missing_duplicates"] ?: 0),
                        label = "С дубликатами",
                        color = Color(0xFF008000)
                    )
                    SummaryItem(
                        count = result.summary["error"] ?: 0,
                        label = "Ошибки",
                        color = Color(0xFF004B23)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Время обработки: ${result.processingTime} сек.",
                color = Color(0xFF008000),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun ToolsSection(
    title: String,
    tools: List<String>,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    color = Color(0xFF004B23),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            tools.forEach { tool ->
                Text(
                    text = "• $tool",
                    color = Color(0xFF008000),
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun DetectionItemView(detection: com.example.toolrecognition.data.models.DetectionItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF38B000).copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = detection.className,
                color = Color(0xFF004B23),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${(detection.confidence * 100).toInt()}%",
                color = Color(0xFF008000),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun SummaryItem(
    count: Int,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count.toString(),
            color = color,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = Color(0xFF004B23),
            fontSize = 12.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

private fun getStatusColor(status: String): Color {
    return when (status) {
        "complete" -> Color(0xFF008000)
        "missing" -> Color(0xFF38B000)
        "extra" -> Color(0xFF008000)
        "mixed" -> Color(0xFF38B000)
        "duplicates" -> Color(0xFF008000)
        "duplicates_only" -> Color(0xFF008000)
        "missing_duplicates" -> Color(0xFF38B000)
        "error" -> Color(0xFF004B23)
        else -> Color(0xFF004B23)
    }
}