package com.example.toolrecognition.data.repository

import android.content.Context
import android.graphics.Bitmap
import com.example.toolrecognition.data.local.SavedAnalysisDao
import com.example.toolrecognition.data.local.SavedAnalysisEntity
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class SavedResultsRepository @Inject constructor(
    private val dao: SavedAnalysisDao,
    private val context: Context
) {

    private fun saveBitmapLocally(bitmap: Bitmap): String {
        val filename = "annotated_${System.currentTimeMillis()}.png"
        val file = File(context.filesDir, "saved_images")
        if (!file.exists()) {
            file.mkdirs()
        }
        val imageFile = File(file, filename)
        val fos = FileOutputStream(imageFile)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        fos.flush()
        fos.close()
        return imageFile.absolutePath
    }

    suspend fun saveSingleResult(
        name: String,
        description: String?,
        resultJson: String,
        annotatedBitmap: Bitmap?
    ): Long {
        val localPath = annotatedBitmap?.let { saveBitmapLocally(it) }

        val entity = SavedAnalysisEntity(
            name = name,
            description = description,
            timestamp = System.currentTimeMillis(),
            singleAnalysisJson = resultJson,
            localAnnotatedImagePath = localPath
        )

        return dao.insert(entity)
    }

    suspend fun saveBatchResult(
        name: String,
        description: String?,
        resultJson: String,
        annotatedBitmaps: List<Bitmap>?
    ): Long {
        val paths = annotatedBitmaps?.map { saveBitmapLocally(it) }

        val entity = SavedAnalysisEntity(
            name = name,
            description = description,
            timestamp = System.currentTimeMillis(),
            batchAnalysisJson = resultJson,
            localAnnotatedImagesBatch = paths
        )

        return dao.insert(entity)
    }

    suspend fun delete(entity: SavedAnalysisEntity) {
        deleteImageFiles(entity)
        dao.delete(entity)
    }

    private fun deleteImageFiles(entity: SavedAnalysisEntity) {
        try {
            entity.localAnnotatedImagePath?.let { path ->
                val file = File(path)
                if (file.exists()) {
                    file.delete()
                }
            }

            entity.localAnnotatedImagesBatch?.forEach { path ->
                val file = File(path)
                if (file.exists()) {
                    file.delete()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getAllSaved(): Flow<List<SavedAnalysisEntity>> = dao.getAllSaved()

    fun observeAll(): Flow<List<SavedAnalysisEntity>> = dao.observeAll()

    suspend fun getById(id: Long): SavedAnalysisEntity? = dao.getById(id)

    @Deprecated("Use delete(entity: SavedAnalysisEntity) instead")
    suspend fun deleteOld(entity: SavedAnalysisEntity) = dao.delete(entity)
}