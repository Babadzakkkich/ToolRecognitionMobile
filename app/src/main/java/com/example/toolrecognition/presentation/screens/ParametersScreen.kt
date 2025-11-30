package com.example.toolrecognition.presentation.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.toolrecognition.presentation.viewmodels.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParametersScreen(
    onNavigateBack: () -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // TopAppBar без Scaffold
        TopAppBar(
            title = {
                Text(
                    text = "Параметры модели",
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
            )
        )

        // Контент
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Confidence Slider
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
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Уверенность",
                            tint = Color(0xFF38B000),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Уверенность (confidence)",
                            color = Color(0xFF004B23),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Текущее значение: ${String.format("%.2f", uiState.confidence)}",
                        color = Color(0xFF004B23),
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Slider(
                        value = uiState.confidence,
                        onValueChange = { newConfidence ->
                            viewModel.updateParameters(newConfidence, uiState.iou)
                        },
                        valueRange = 0f..1f,
                        steps = 19
                    )

                    Text(
                        text = "Более высокое значение = меньше ложных срабатываний, но можно пропустить объекты",
                        color = Color(0xFF008000).copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            }

            // IoU Slider
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
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Пересечение",
                            tint = Color(0xFF38B000),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Пересечение (IoU)",
                            color = Color(0xFF004B23),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Текущее значение: ${String.format("%.2f", uiState.iou)}",
                        color = Color(0xFF004B23),
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Slider(
                        value = uiState.iou,
                        onValueChange = { newIou ->
                            viewModel.updateParameters(uiState.confidence, newIou)
                        },
                        valueRange = 0f..1f,
                        steps = 19
                    )

                    Text(
                        text = "Порог для подавления дублирующих обнаружений",
                        color = Color(0xFF008000).copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            }

            // Рекомендации
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
                        text = "Рекомендации по настройке:",
                        color = Color(0xFF004B23),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "• Для четких изображений: confidence = 0.5-0.7",
                        color = Color(0xFF008000),
                        fontSize = 14.sp
                    )
                    Text(
                        text = "• Для сложных условий: confidence = 0.2-0.4",
                        color = Color(0xFF008000),
                        fontSize = 14.sp
                    )
                    Text(
                        text = "• Баланс скорости/точности: IoU = 0.4-0.6",
                        color = Color(0xFF008000),
                        fontSize = 14.sp
                    )
                    Text(
                        text = "• Высокая точность: IoU = 0.6-0.8",
                        color = Color(0xFF008000),
                        fontSize = 14.sp
                    )
                }
            }

            // Кнопка сохранения
            Button(
                onClick = onNavigateBack,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF38B000)
                )
            ) {
                Text("Готово", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}