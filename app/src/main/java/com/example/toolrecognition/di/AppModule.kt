package com.example.toolrecognition.di

import com.example.toolrecognition.data.api.ApiService
import com.example.toolrecognition.data.api.RetrofitInstance
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object AppModule {

    @Provides
    @ViewModelScoped
    fun provideApiService(): ApiService {
        return RetrofitInstance.api
    }
}