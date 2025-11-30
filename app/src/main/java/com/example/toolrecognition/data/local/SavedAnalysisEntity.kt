package com.example.toolrecognition.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_analysis")
data class SavedAnalysisEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,
    val description: String?,
    val timestamp: Long,

    // JSON результатов
    val singleAnalysisJson: String? = null,
    val batchAnalysisJson: String? = null,

    // --- Новые поля для оффлайн изображений ---
    val localAnnotatedImagePath: String? = null,              // одиночное изображение
    val localAnnotatedImagesBatch: List<String>? = null       // batch изображения
)