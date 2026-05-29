package com.example.downloader.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.downloader.ui.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Settings", style = MaterialTheme.typography.titleLarge)
        ElevatedCard {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Download folder: ${state.downloadFolder}")
                Text("Theme: ${if (state.darkTheme) "Dark" else "Light"}")
            }
        }
        ElevatedCard {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Default quality")
                QualityPicker(state.defaultQuality, viewModel::setDefaultQuality)
            }
        }
    }
}

@Composable
private fun QualityPicker(current: String, onSelect: (String) -> Unit) {
    val options = listOf("144p", "360p", "720p", "1080p")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { quality ->
            FilterChip(
                selected = current == quality,
                onClick = { onSelect(quality) },
                label = { Text(quality) }
            )
        }
    }
}
