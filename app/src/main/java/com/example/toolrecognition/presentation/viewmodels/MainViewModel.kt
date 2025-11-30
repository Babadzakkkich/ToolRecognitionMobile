package com.example.toolrecognition.presentation.viewmodels

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.toolrecognition.data.api.ApiService
import com.example.toolrecognition.data.local.SavedAnalysisEntity
import com.example.toolrecognition.data.models.BatchAnalysisResponse
import com.example.toolrecognition.data.models.SingleAnalysisResponse
import com.example.toolrecognition.data.repository.SavedResultsRepository
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
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
    private val apiService: ApiService,
    private val savedResultsRepository: SavedResultsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    // Прогресс загрузки batch изображений
    private val _batchDownloadProgress = MutableStateFlow(BatchDownloadProgress())
    val batchDownloadProgress: StateFlow<BatchDownloadProgress> = _batchDownloadProgress.asStateFlow()

    private val gson = Gson()

    // -----------------------------
    //         PARAMS & IMAGE
    // -----------------------------

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

    // -----------------------------
    //       SINGLE ANALYSIS
    // -----------------------------

    fun analyzeSingleImage() {
        val bitmap = _uiState.value.selectedImageBitmap
        if (bitmap == null) {
            _uiState.value = _uiState.value.copy(error = "Пожалуйста, выберите изображение")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val tempFile = File.createTempFile("image", ".jpg")
                FileOutputStream(tempFile).use { fos ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                }

                val requestFile = tempFile.asRequestBody("image/jpeg".toMediaType())
                val body = MultipartBody.Part.createFormData("file", "image.jpg", requestFile)

                val response = apiService.analyzeSingleImage(
                    file = body,
                    confidence = _uiState.value.confidence,
                    iou = _uiState.value.iou
                )

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

    // -----------------------------
    //         BATCH ANALYSIS
    // -----------------------------

    fun analyzeBatchImages(inputStream: InputStream, filename: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val bytes = inputStream.readBytes()

                val tempFile = File.createTempFile("batch", ".zip")
                FileOutputStream(tempFile).use { it.write(bytes) }

                val requestFile = tempFile.asRequestBody("application/zip".toMediaType())

                val body = MultipartBody.Part.createFormData("file", filename, requestFile)

                val response = apiService.analyzeBatchImages(
                    file = body,
                    confidence = _uiState.value.confidence,
                    iou = _uiState.value.iou
                )

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

    // -----------------------------
    //           CLEARING
    // -----------------------------

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearResults() {
        _uiState.value = _uiState.value.copy(
            singleAnalysisResult = null,
            batchAnalysisResult = null,
            selectedImageUri = null,
            selectedImageBitmap = null
        )
    }

    fun resetAnalysisState() {
        _uiState.value = _uiState.value.copy(
            selectedImageUri = null,
            selectedImageBitmap = null,
            singleAnalysisResult = null,
            batchAnalysisResult = null,
            error = null,
            savedOperationResult = null
        )
        // Сбрасываем прогресс
        _batchDownloadProgress.value = BatchDownloadProgress()
    }

    // -----------------------------
    //   SAVING TO LOCAL DATABASE
    // -----------------------------

    fun saveCurrentResult(name: String, description: String?) {
        val single = _uiState.value.singleAnalysisResult
        val batch = _uiState.value.batchAnalysisResult

        if (single == null && batch == null) {
            _uiState.value = _uiState.value.copy(error = "Нет результатов для сохранения")
            return
        }

        viewModelScope.launch {
            try {
                val id = if (single != null) {
                    // Сохраняем одиночный результат
                    val singleJson = gson.toJson(single)
                    savedResultsRepository.saveSingleResult(
                        name = name,
                        description = description,
                        resultJson = singleJson,
                        annotatedBitmap = _uiState.value.selectedImageBitmap
                    )
                } else {
                    // Сохраняем batch результат - ЗАГРУЖАЕМ РАЗМЕЧЕННЫЕ ИЗОБРАЖЕНИЯ с прогрессом
                    val batchJson = gson.toJson(batch)
                    val annotatedBitmaps = loadBatchAnnotatedImagesWithProgress(batch!!)

                    savedResultsRepository.saveBatchResult(
                        name = name,
                        description = description,
                        resultJson = batchJson,
                        annotatedBitmaps = annotatedBitmaps
                    )
                }

                _uiState.value = _uiState.value.copy(savedOperationResult = "Сохранено (id=$id)")

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Ошибка сохранения: ${e.message}")
            } finally {
                // Сбрасываем прогресс после завершения
                _batchDownloadProgress.value = BatchDownloadProgress()
            }
        }
    }

    // -----------------------------
    //   ЗАГРУЗКА РАЗМЕЧЕННЫХ ИЗОБРАЖЕНИЙ ДЛЯ BATCH С ПРОГРЕССОМ
    // -----------------------------

    private suspend fun loadBatchAnnotatedImagesWithProgress(batchResult: BatchAnalysisResponse): List<Bitmap> {
        val imagePaths = batchResult.results.mapNotNull { it.annotatedImagePath }

        if (imagePaths.isEmpty()) return emptyList()

        // Инициализируем прогресс
        _batchDownloadProgress.value = BatchDownloadProgress(
            isDownloading = true,
            totalImages = imagePaths.size,
            downloadedImages = 0,
            currentProgress = 0f
        )

        val bitmaps = mutableListOf<Bitmap>()

        // Разбиваем на группы по 5 изображений для параллельной загрузки
        val chunks = imagePaths.chunked(5)

        for ((chunkIndex, chunk) in chunks.withIndex()) {
            // Загружаем текущую группу параллельно
            val chunkResults = chunk.map { path ->
                viewModelScope.async {
                    try {
                        val imageUrl = buildImageUrl(path)
                        val response = apiService.getImage(imageUrl)
                        if (response.isSuccessful) {
                            response.body()?.byteStream()?.use { inputStream ->
                                BitmapFactory.decodeStream(inputStream)
                            }
                        } else {
                            null
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                }
            }.awaitAll()

            // Добавляем успешно загруженные изображения
            chunkResults.filterNotNull().forEach { bitmap ->
                bitmaps.add(bitmap)
            }

            // Обновляем прогресс
            val downloaded = (chunkIndex + 1) * chunk.size
            val progress = (downloaded.toFloat() / imagePaths.size).coerceAtMost(1f)

            _batchDownloadProgress.value = BatchDownloadProgress(
                isDownloading = true,
                totalImages = imagePaths.size,
                downloadedImages = downloaded.coerceAtMost(imagePaths.size),
                currentProgress = progress
            )
        }

        return bitmaps
    }

    // Старая версия для совместимости
    private suspend fun loadBatchAnnotatedImages(batchResult: BatchAnalysisResponse): List<Bitmap> {
        return loadBatchAnnotatedImagesWithProgress(batchResult)
    }

    private fun buildImageUrl(path: String): String {
        val clean = path.replace("\\", "/").removePrefix("results/")
        return "http://10.0.2.2:8000/tools/images/$clean"
    }

    fun observeSavedResults(): Flow<List<SavedAnalysisEntity>> =
        savedResultsRepository.observeAll()

    suspend fun getSavedById(id: Long): SavedAnalysisEntity? =
        savedResultsRepository.getById(id)

    fun deleteSaved(entity: SavedAnalysisEntity) {
        viewModelScope.launch {
            savedResultsRepository.delete(entity)
        }
    }

    // Отмена загрузки
    fun cancelBatchDownload() {
        _batchDownloadProgress.value = BatchDownloadProgress()
    }
}

// -----------------------------
//          UI STATE
// -----------------------------

data class MainUiState(
    val confidence: Float = 0.5f,
    val iou: Float = 0.45f,
    val selectedImageUri: Uri? = null,
    val selectedImageBitmap: Bitmap? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val singleAnalysisResult: SingleAnalysisResponse? = null,
    val batchAnalysisResult: BatchAnalysisResponse? = null,
    val savedOperationResult: String? = null
)

// Прогресс загрузки batch изображений
data class BatchDownloadProgress(
    val isDownloading: Boolean = false,
    val totalImages: Int = 0,
    val downloadedImages: Int = 0,
    val currentProgress: Float = 0f
) {
    val progressPercent: Int get() = (currentProgress * 100).toInt()
}