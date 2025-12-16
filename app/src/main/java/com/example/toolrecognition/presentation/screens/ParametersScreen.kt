package com.example.toolrecognition.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.toolrecognition.presentation.components.ParametersPanel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParametersScreen(
    confidence: Float,
    iou: Float,
    onConfidenceChange: (Float) -> Unit,
    onIouChange: (Float) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            ParametersPanel(
                confidence = confidence,
                iou = iou,
                onConfidenceChange = onConfidenceChange,
                onIouChange = onIouChange,
                modifier = Modifier.fillMaxWidth()
            )

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