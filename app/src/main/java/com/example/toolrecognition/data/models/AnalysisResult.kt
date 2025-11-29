package com.example.toolrecognition.data.models

import com.google.gson.annotations.SerializedName

data class AnalysisResult(
    @SerializedName("status")
    val status: String,

    @SerializedName("total_detections")
    val totalDetections: Int,

    @SerializedName("expected_count")
    val expectedCount: Int = 11,

    @SerializedName("missing_tools")
    val missingTools: List<String> = emptyList(),

    @SerializedName("extra_tools")
    val extraTools: List<String> = emptyList(),

    @SerializedName("detected_tools")
    val detectedTools: List<String> = emptyList(),

    @SerializedName("detections")
    val detections: List<DetectionItem>,

    @SerializedName("message")
    val message: String
)