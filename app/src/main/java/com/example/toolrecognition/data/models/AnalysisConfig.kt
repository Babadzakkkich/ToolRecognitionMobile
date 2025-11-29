package com.example.toolrecognition.data.models

import com.google.gson.annotations.SerializedName

data class AnalysisConfig(
    @SerializedName("confidence_threshold")
    val confidenceThreshold: Float,

    @SerializedName("iou_threshold")
    val iouThreshold: Float,

    @SerializedName("annotated_image_path")
    val annotatedImagePath: String? = null,

    @SerializedName("output_directory")
    val outputDirectory: String? = null,

    @SerializedName("total_annotated_images")
    val totalAnnotatedImages: Int? = null
)