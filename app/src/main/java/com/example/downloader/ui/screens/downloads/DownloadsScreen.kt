package com.example.downloader.ui.screens.downloads

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.WorkInfo
import com.example.downloader.ui.viewmodel.DownloadsViewModel
import com.example.downloader.workers.DownloadWorker

@Composable
fun DownloadsScreen(viewModel: DownloadsViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Text("Active downloads", style = MaterialTheme.typography.titleMedium) }
        items(state.active, key = { it.id }) { item -> DownloadItem(item) }
        item { Text("Completed", style = MaterialTheme.typography.titleMedium) }
        items(state.completed, key = { it.id }) { item -> DownloadItem(item) }
        item { Text("Failed", style = MaterialTheme.typography.titleMedium) }
        items(state.failed, key = { it.id }) { item -> FailedItem(item) { viewModel.retry(item.id) } }
    }
}

@Composable
private fun DownloadItem(item: WorkInfo) {
    ElevatedCard {
        val progress = item.progress.getInt(DownloadWorker.KEY_PROGRESS, 0)
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(text = item.id.toString(), style = MaterialTheme.typography.bodySmall)
            LinearProgressIndicator(progress = progress / 100f, modifier = Modifier.fillMaxWidth())
            Text("State: ${item.state.name}")
        }
    }
}

@Composable
private fun FailedItem(item: WorkInfo, onRetry: () -> Unit) {
    ElevatedCard {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Failed: ${item.id}")
            TextButton(onClick = onRetry) { Text("Retry") }
        }
    }
}
