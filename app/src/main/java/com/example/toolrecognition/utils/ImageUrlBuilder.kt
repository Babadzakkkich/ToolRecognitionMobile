package com.example.toolrecognition.utils

import com.example.toolrecognition.data.config.AppConfig

object ImageUrlBuilder {

    fun buildImageUrl(path: String): String {
        val cleanPath = path
            .replace("\\", "/")
            .removePrefix("results/")
        return "${AppConfig.IMAGES_BASE_URL}$cleanPath"
    }

    fun buildAnnotatedImageUrl(annotatedImagePath: String): String {
        val cleanPath = annotatedImagePath
            .replace("\\", "/")
            .removePrefix("results/")
        return "${AppConfig.IMAGES_BASE_URL}$cleanPath"
    }
}