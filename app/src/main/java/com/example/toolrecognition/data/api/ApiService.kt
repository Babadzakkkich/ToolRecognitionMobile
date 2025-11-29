package com.example.toolrecognition.data.api

import com.example.toolrecognition.data.models.BatchAnalysisResponse
import com.example.toolrecognition.data.models.SingleAnalysisResponse
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query
import retrofit2.http.Streaming
import retrofit2.http.Url

interface ApiService {
    @Multipart
    @POST("tools/analyze")
    suspend fun analyzeSingleImage(
        @Part file: MultipartBody.Part,
        @Query("confidence") confidence: Float,
        @Query("iou") iou: Float
    ): SingleAnalysisResponse

    @Multipart
    @POST("tools/analyze-batch")
    suspend fun analyzeBatchImages(
        @Part file: MultipartBody.Part,
        @Query("confidence") confidence: Float,
        @Query("iou") iou: Float
    ): BatchAnalysisResponse

    @GET
    @Streaming
    suspend fun getImage(@Url url: String): Response<ResponseBody>
}