package com.example.downloader.ui.screens.downloads

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.WorkInfo
import com.example.downloader.ui.viewmodel.DownloadsViewModel
import com.example.downloader.workers.DownloadWorker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsScreen(
    onBack: () -> Unit,
    viewModel: DownloadsViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Downloads") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            if (state.active.isEmpty() && state.completed.isEmpty() && state.failed.isEmpty()) {
                item {
                    Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No downloads yet", color = MaterialTheme.colorScheme.outline)
                    }
                }
            }

            if (state.active.isNotEmpty()) {
                item { SectionHeader("Active") }
                items(state.active, key = { it.id }) { item -> DownloadItem(item) }
            }

            if (state.completed.isNotEmpty()) {
                item { SectionHeader("Completed") }
                items(state.completed, key = { it.id }) { item -> DownloadItem(item) }
            }

            if (state.failed.isNotEmpty()) {
                item { SectionHeader("Failed") }
                items(state.failed, key = { it.id }) { item -> FailedItem(item) { viewModel.retry(item.id) } }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

@Composable
private fun DownloadItem(item: WorkInfo) {
    val progress = item.progress.getInt(DownloadWorker.KEY_PROGRESS, 0)
    val title = item.progress.getString(DownloadWorker.KEY_TITLE)
        ?: item.outputData.getString(DownloadWorker.KEY_TITLE)
        ?: "Video Download"
    
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (item.state == WorkInfo.State.SUCCEEDED) Icons.Default.CheckCircle else Icons.Default.Download,
                    contentDescription = null,
                    tint = if (item.state == WorkInfo.State.SUCCEEDED) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }
            
            if (item.state == WorkInfo.State.RUNNING || item.state == WorkInfo.State.ENQUEUED) {
                LinearProgressIndicator(
                    progress = { progress / 100f },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Downloading...", style = MaterialTheme.typography.bodySmall)
                    Text(text = "$progress%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }
            } else if (item.state == WorkInfo.State.SUCCEEDED) {
                Text(text = "Finished", style = MaterialTheme.typography.bodySmall, color = Color(0xFF4CAF50))
            }
        }
    }
}

@Composable
private fun FailedItem(item: WorkInfo, onRetry: () -> Unit) {
    val title = item.outputData.getString(DownloadWorker.KEY_TITLE) ?: "Video Download"
    
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.error)),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = title, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
            }
            IconButton(onClick = onRetry) {
                Icon(Icons.Default.Refresh, contentDescription = "Retry", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
