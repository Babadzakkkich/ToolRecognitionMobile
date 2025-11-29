package com.example.toolrecognition.data.models

import com.google.gson.annotations.SerializedName

data class ImageAnalysisResult(
    @SerializedName("filename")
    val filename: String,

    @SerializedName("analysis_result")
    val analysisResult: AnalysisResult,

    @SerializedName("annotated_image_path")
    val annotatedImagePath: String? = null
)