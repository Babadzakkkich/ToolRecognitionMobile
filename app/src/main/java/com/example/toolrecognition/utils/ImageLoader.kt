package com.example.toolrecognition.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.example.toolrecognition.data.api.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun rememberImageBitmapFromUrl(url: String): ImageBitmap? {
    val bitmapState = remember(url) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(url) {
        val bitmap = withContext(Dispatchers.IO) {
            loadBitmapFromUrl(url)
        }
        bitmapState.value = bitmap
    }

    return bitmapState.value
}

private suspend fun loadBitmapFromUrl(url: String): ImageBitmap? {
    return try {
        val response = RetrofitInstance.api.getImage(url)
        if (response.isSuccessful) {
            response.body()?.byteStream()?.use { inputStream ->
                val bitmap = BitmapFactory.decodeStream(inputStream)
                bitmap?.asImageBitmap()
            }
        } else {
            null
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}