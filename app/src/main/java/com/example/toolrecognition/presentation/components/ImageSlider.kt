package com.example.toolrecognition.presentation.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.toolrecognition.data.models.ImageAnalysisResult
import com.example.toolrecognition.utils.ImageUrlBuilder
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImageSlider(
    results: List<ImageAnalysisResult>,
    currentIndex: Int,
    onIndexChange: (Int) -> Unit,
    localImagePaths: List<String>? = null,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(initialPage = currentIndex, pageCount = { results.size })
    val coroutine = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp

    LaunchedEffect(currentIndex) {
        if (pagerState.currentPage != currentIndex) {
            pagerState.animateScrollToPage(currentIndex)
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        val thumbWidth = with(density) { 80.dp.toPx() }
        val spacing = with(density) { 8.dp.toPx() }
        val itemWidth = thumbWidth + spacing
        val screenWidthPx = with(density) { screenWidth.toPx() }

        val targetStart = pagerState.currentPage * itemWidth
        val targetEnd = targetStart + itemWidth

        // Добавляем небольшой отступ (20% от ширины элемента) для более ранней прокрутки
        val buffer = itemWidth * 0.2f

        val visibleStart = scrollState.value.toFloat()
        val visibleEnd = visibleStart + screenWidthPx

        // Условие для прокрутки влево (когда миниатюра начинает скрываться слева)
        val needsScrollLeft = targetStart < (visibleStart + buffer)

        // Условие для прокрутки вправо (когда миниатюра начинает скрываться справа)
        val needsScrollRight = targetEnd > (visibleEnd - buffer)

        if (!needsScrollLeft && !needsScrollRight) {
            onIndexChange(pagerState.currentPage)
            return@LaunchedEffect
        }

        val newScroll = when {
            needsScrollLeft -> {
                // Для прокрутки влево - показываем миниатюру с небольшим отступом
                (targetStart - buffer).toInt()
            }
            needsScrollRight -> {
                // Для прокрутки вправо - центрируем миниатюру
                val centerPosition = targetStart - (screenWidthPx - itemWidth) / 2
                centerPosition.toInt()
            }
            else -> scrollState.value
        }

        // Ограничиваем значения, чтобы не выйти за границы
        val maxScroll = (results.size * itemWidth - screenWidthPx).toInt()
        val clampedScroll = newScroll.coerceIn(0, maxScroll.coerceAtLeast(0))

        scrollState.animateScrollTo(clampedScroll)
        onIndexChange(pagerState.currentPage)
    }

    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SliderControls(
                index = pagerState.currentPage,
                total = results.size,
                filename = results.getOrNull(pagerState.currentPage)?.filename.orEmpty(),
                onPrev = {
                    if (pagerState.currentPage > 0) {
                        coroutine.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    }
                },
                onNext = {
                    if (pagerState.currentPage < results.size - 1) {
                        coroutine.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                }
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    val result = results[page]

                    // Проверяем есть ли локальное изображение
                    val localImagePath = localImagePaths?.getOrNull(page)

                    if (localImagePath != null) {
                        // Показываем локальное изображение
                        val bitmap = remember(localImagePath) {
                            BitmapFactory.decodeFile(localImagePath)
                        }

                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = result.filename,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            MissingImageView(result.filename)
                        }
                    } else {
                        // Используем URL изображения
                        val url = result.annotatedImagePath?.let { ImageUrlBuilder.buildImageUrl(it) }

                        if (url != null) {
                            ImageWithLoader(
                                imageUrl = url,
                                contentDescription = result.filename,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            MissingImageView(result.filename)
                        }
                    }
                }
            }

            Thumbnails(
                results = results,
                currentIndex = pagerState.currentPage,
                scrollState = scrollState,
                onClick = { index ->
                    coroutine.launch { pagerState.animateScrollToPage(index) }
                }
            )
        }
    }
}

@Composable
private fun SliderControls(
    index: Int,
    total: Int,
    filename: String,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ControlButton(index > 0, Icons.AutoMirrored.Filled.ArrowBack, onPrev)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Изображение ${index + 1} из $total", color = Color(0xFF004B23), fontSize = 16.sp)
            Text(filename, color = Color(0xFF008000), fontSize = 12.sp, maxLines = 1)
        }
        ControlButton(index < total - 1, Icons.AutoMirrored.Filled.ArrowForward, onNext)
    }
}

@Composable
private fun ControlButton(enabled: Boolean, icon: ImageVector, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(Color(0xFF006400)),
        modifier = Modifier.size(64.dp)
    ) {
        Icon(
            icon,
            null,
            tint = Color.White,
            modifier = Modifier.size(30.dp)
        )
    }
}

@Composable
private fun Thumbnails(
    results: List<ImageAnalysisResult>,
    currentIndex: Int,
    scrollState: androidx.compose.foundation.ScrollState,
    onClick: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        results.forEachIndexed { index, item ->
            Thumbnail(
                result = item,
                index = index,
                selected = index == currentIndex,
                onClick = { onClick(index) }
            )
        }
    }
}

@Composable
private fun Thumbnail(
    result: ImageAnalysisResult,
    index: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    val analysis = result.analysisResult

    Card(
        modifier = Modifier
            .size(80.dp, 90.dp)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) Color(0xFF38B000) else Color(0x33008000),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(if (selected) 4.dp else 1.dp),
        colors = CardDefaults.cardColors(
            if (selected) Color(0xFF38B000).copy(alpha = 0.1f) else Color.White
        )
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(6.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(
                    getStatusIcon(analysis.status),
                    null,
                    tint = getStatusColor(analysis.status),
                    modifier = Modifier.size(20.dp)
                )
                Text("${analysis.totalDetections} обн.", color = Color(0xFF004B23), fontSize = 10.sp)
                Box(
                    modifier = Modifier.size(20.dp).clip(CircleShape).background(Color(0xFF38B000).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("${index + 1}", color = Color(0xFF004B23), fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
private fun MissingImageView(name: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(Icons.Default.Warning, null, tint = Color(0xFF38B000), modifier = Modifier.size(48.dp))
        Text("Размеченное изображение недоступно", color = Color(0xFF004B23), fontSize = 14.sp)
        Text(name, color = Color(0xFF008000), fontSize = 12.sp)
    }
}

private fun getStatusIcon(status: String) = when (status) {
    "complete" -> Icons.Default.CheckCircle
    "missing" -> Icons.Default.Warning
    "missing_duplicates" -> Icons.Default.Warning
    "error" -> Icons.Default.Error
    else -> Icons.Default.Warning
}

private fun getStatusColor(status: String) = when (status) {
    "complete" -> Color(0xFF008000)
    "missing" -> Color(0xFF38B000)
    "missing_duplicates" -> Color(0xFF008000)
    "error" -> Color(0xFF004B23)
    else -> Color(0xFF38B000)
}