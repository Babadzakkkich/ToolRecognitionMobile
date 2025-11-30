package com.example.toolrecognition.di

import android.content.Context
import androidx.room.Room
import com.example.toolrecognition.data.local.AppDatabase
import com.example.toolrecognition.data.local.SavedAnalysisDao
import com.example.toolrecognition.data.repository.SavedResultsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object LocalModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext appContext: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "saved_results.db"
        ).fallbackToDestructiveMigration() // Добавляем для миграции
            .build()
    }

    @Provides
    fun provideSavedAnalysisDao(db: AppDatabase): SavedAnalysisDao = db.savedAnalysisDao()

    @Provides
    @Singleton
    fun provideSavedResultsRepository(
        dao: SavedAnalysisDao,
        @ApplicationContext context: Context
    ): SavedResultsRepository = SavedResultsRepository(dao, context)
}