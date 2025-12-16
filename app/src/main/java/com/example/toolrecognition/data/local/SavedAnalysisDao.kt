package com.example.toolrecognition.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedAnalysisDao {
    @Query("SELECT * FROM saved_analysis ORDER BY timestamp DESC")
    fun getAllSaved(): Flow<List<SavedAnalysisEntity>>

    @Query("SELECT * FROM saved_analysis WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): SavedAnalysisEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: SavedAnalysisEntity): Long

    @Delete
    suspend fun delete(entity: SavedAnalysisEntity)

    @Query("SELECT * FROM saved_analysis ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<SavedAnalysisEntity>>
}