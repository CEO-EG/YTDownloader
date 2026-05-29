package com.example.downloader.ui.screens.details

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.downloader.data.model.VideoFormat
import com.example.downloader.ui.viewmodel.DetailsViewModel

@Composable
fun DetailsScreen(
    url: String,
    viewModel: DetailsViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(url) {
        viewModel.loadMetadata(url)
        viewModel.loadFormats(url)
    }
    if (state.loadingMetadata && state.videoInfo == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }
    if (state.error != null && state.videoInfo == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            Text(text = state.error ?: "Unknown error")
        }
        return
    }
    val videoInfo = state.videoInfo ?: return

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ElevatedCard(shape = MaterialTheme.shapes.large) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AsyncImage(
                        model = videoInfo.thumbnail,
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth().height(220.dp)
                    )
                    Text(videoInfo.title, style = MaterialTheme.typography.titleLarge)
                    Text("Duration: ${videoInfo.duration}s")
                    Text("Channel: ${videoInfo.channelName}")
                    Text("Views: ${videoInfo.views}")
                }
            }
        }
        item {
            Text("Formats", style = MaterialTheme.typography.titleMedium)
        }
        if (state.loadingFormats) {
            item { LinearProgressIndicator(modifier = Modifier.fillMaxWidth()) }
        }
        items(state.formats, key = { it.formatId }) { format ->
            FormatRow(
                format = format,
                selected = state.selectedFormat?.formatId == format.formatId,
                onClick = { viewModel.selectFormat(format) }
            )
        }
        item {
            Button(
                onClick = { viewModel.download(url) },
                modifier = Modifier.fillMaxWidth(),
                enabled = state.selectedFormat != null
            ) { Text("Download") }
        }
    }
}

@Composable
private fun FormatRow(format: VideoFormat, selected: Boolean, onClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(format.quality)
            Text("${format.extension} • ${format.type}")
        }
    }
}