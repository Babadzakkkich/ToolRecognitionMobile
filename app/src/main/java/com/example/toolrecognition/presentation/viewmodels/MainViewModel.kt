package com.example.toolrecognition.presentation.viewmodels

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.toolrecognition.data.api.ApiService
import com.example.toolrecognition.data.models.BatchAnalysisResponse
import com.example.toolrecognition.data.models.SingleAnalysisResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    fun updateParameters(confidence: Float, iou: Float) {
        _uiState.value = _uiState.value.copy(
            confidence = confidence,
            iou = iou
        )
    }

    fun setSelectedImage(uri: Uri?, bitmap: Bitmap?) {
        _uiState.value = _uiState.value.copy(
            selectedImageUri = uri,
            selectedImageBitmap = bitmap
        )
    }

    fun analyzeSingleImage() {
        val bitmap = _uiState.value.selectedImageBitmap
        if (bitmap == null) {
            _uiState.value = _uiState.value.copy(
                error = "Пожалуйста, выберите изображение"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            try {
                // Создаем временный файл для изображения
                val tempFile = File.createTempFile("image", ".jpg")
                val outputStream = FileOutputStream(tempFile)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.close()

                // Создаем RequestBody используя новый API
                val requestFile = tempFile.asRequestBody("image/jpeg".toMediaType())

                val body = MultipartBody.Part.createFormData(
                    "file",
                    "image.jpg",
                    requestFile
                )

                val response = apiService.analyzeSingleImage(
                    file = body,
                    confidence = _uiState.value.confidence,
                    iou = _uiState.value.iou
                )

                // Удаляем временный файл
                tempFile.delete()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    singleAnalysisResult = response,
                    batchAnalysisResult = null
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Ошибка: ${e.message}"
                )
            }
        }
    }

    fun analyzeBatchImages(inputStream: InputStream, filename: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            try {
                val bytes = inputStream.readBytes()

                // Создаем временный файл для ZIP архива
                val tempFile = File.createTempFile("batch", ".zip")
                FileOutputStream(tempFile).use { fos ->
                    fos.write(bytes)
                }

                // Создаем RequestBody используя новый API
                val requestFile = tempFile.asRequestBody("application/zip".toMediaType())

                val body = MultipartBody.Part.createFormData(
                    "file",
                    filename,
                    requestFile
                )

                val response = apiService.analyzeBatchImages(
                    file = body,
                    confidence = _uiState.value.confidence,
                    iou = _uiState.value.iou
                )

                // Удаляем временный файл
                tempFile.delete()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    batchAnalysisResult = response,
                    singleAnalysisResult = null
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Ошибка: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearResults() {
        _uiState.value = _uiState.value.copy(
            singleAnalysisResult = null,
            batchAnalysisResult = null,
            // Очищаем также выбранное изображение при очистке результатов
            selectedImageUri = null,
            selectedImageBitmap = null
        )
    }

    // Новая функция для полного сброса состояния анализа
    fun resetAnalysisState() {
        _uiState.value = _uiState.value.copy(
            selectedImageUri = null,
            selectedImageBitmap = null,
            singleAnalysisResult = null,
            batchAnalysisResult = null,
            error = null
        )
    }
}

data class MainUiState(
    val confidence: Float = 0.5f,
    val iou: Float = 0.45f,
    val selectedImageUri: Uri? = null,
    val selectedImageBitmap: Bitmap? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val singleAnalysisResult: SingleAnalysisResponse? = null,
    val batchAnalysisResult: BatchAnalysisResponse? = null
)