package com.example.toolrecognition.data.models

import com.google.gson.annotations.SerializedName

data class DetectionItem(
    @SerializedName("class_id")
    val classId: Int,

    @SerializedName("class_name")
    val className: String,

    @SerializedName("confidence")
    val confidence: Float,

    @SerializedName("bbox")
    val bbox: List<Float>
)
