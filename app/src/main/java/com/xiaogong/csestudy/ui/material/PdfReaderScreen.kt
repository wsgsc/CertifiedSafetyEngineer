package com.xiaogong.csestudy.ui.material

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import com.github.barteksc.pdfviewer.util.FitPolicy
import com.xiaogong.csestudy.util.PdfAssets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfReaderScreen(assetName: String, onBack: () -> Unit) {
    val context = LocalContext.current
    var file by remember { mutableStateOf<File?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var pageCount by remember { mutableIntStateOf(0) }
    // 转屏后回到原来那一页
    var currentPage by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(assetName) {
        runCatching { withContext(Dispatchers.IO) { PdfAssets.ensureLocalFile(context, assetName) } }
            .onSuccess { file = it }
            .onFailure { error = it.message ?: "打开失败" }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(PdfAssets.titleOf(assetName), maxLines = 1, overflow = TextOverflow.Ellipsis)
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "返回",
                        modifier = Modifier.rotate(180f))
                }
            }
        )
        Box(modifier = Modifier.fillMaxSize()) {
            val loaded = file
            when {
                error != null -> Text(
                    text = error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
                loaded == null -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                else -> {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { ctx ->
                            PDFView(ctx, null).apply {
                                fromFile(loaded)
                                    .defaultPage(currentPage)
                                    .enableSwipe(true)
                                    .enableDoubletap(true)
                                    .enableAnnotationRendering(true)
                                    .pageFitPolicy(FitPolicy.WIDTH)
                                    .pageSnap(false)
                                    .autoSpacing(false)
                                    .spacing(6)
                                    .scrollHandle(DefaultScrollHandle(ctx))
                                    .onLoad { total -> pageCount = total }
                                    .onPageChange { page, _ -> currentPage = page }
                                    .onError { error = it.message ?: "渲染失败" }
                                    .load()
                            }
                        }
                    )
                    if (pageCount > 0) {
                        PageBadge(
                            text = "${currentPage + 1} / $pageCount",
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PageBadge(text: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = Color.Black.copy(alpha = 0.55f)
    ) {
        Text(
            text = text,
            color = Color.White,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}
