package com.example.toolrecognition.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.example.toolrecognition.data.models.SingleAnalysisResponse
import com.example.toolrecognition.data.models.BatchAnalysisResponse

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun singleAnalysisFromJson(json: String?): SingleAnalysisResponse? {
        return if (json.isNullOrEmpty()) null
        else gson.fromJson(json, SingleAnalysisResponse::class.java)
    }

    @TypeConverter
    fun singleAnalysisToJson(obj: SingleAnalysisResponse?): String? {
        return obj?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun batchAnalysisFromJson(json: String?): BatchAnalysisResponse? {
        return if (json.isNullOrEmpty()) null
        else gson.fromJson(json, BatchAnalysisResponse::class.java)
    }

    @TypeConverter
    fun batchAnalysisToJson(obj: BatchAnalysisResponse?): String? {
        return obj?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun mapFromJson(json: String?): Map<String, Int>? {
        if (json.isNullOrEmpty()) return null
        val type = object : TypeToken<Map<String, Int>>() {}.type
        return gson.fromJson(json, type)
    }

    @TypeConverter
    fun mapToJson(map: Map<String, Int>?): String? = map?.let { gson.toJson(it) }

    @TypeConverter
    fun fromStringList(list: List<String>?): String? =
        list?.joinToString(";;")

    @TypeConverter
    fun toStringList(data: String?): List<String>? =
        data?.split(";;")?.filter { it.isNotBlank() }
}