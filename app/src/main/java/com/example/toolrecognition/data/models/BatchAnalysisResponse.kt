package com.example.toolrecognition.data.models

import com.google.gson.annotations.SerializedName

data class BatchAnalysisResponse(
    @SerializedName("status")
    val status: String,

    @SerializedName("total_images")
    val totalImages: Int,

    @SerializedName("processed_images")
    val processedImages: Int,

    @SerializedName("results")
    val results: List<ImageAnalysisResult>,

    @SerializedName("processing_time")
    val processingTime: Float,

    @SerializedName("summary")
    val summary: Map<String, Int>,

    @SerializedName("config")
    val config: AnalysisConfig? = null
)