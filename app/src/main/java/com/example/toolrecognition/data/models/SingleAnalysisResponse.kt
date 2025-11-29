package com.example.toolrecognition.data.models

import com.google.gson.annotations.SerializedName

data class SingleAnalysisResponse(
    @SerializedName("status")
    val status: String,

    @SerializedName("analysis_result")
    val analysisResult: AnalysisResult,

    @SerializedName("config")
    val config: AnalysisConfig
)