package com.example.downloader.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.downloader.ui.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    onContinueClick: (String) -> Unit,
    onDownloadsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val clipboardManager = LocalClipboardManager.current
    val urlState by viewModel.url.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "YouTube Downloader",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(
            value = urlState,
            onValueChange = viewModel::setUrl,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = "Paste YouTube URL") },
            singleLine = true
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = {
                    val clipboardText = clipboardManager.getText()?.text?.toString()
                    if (!clipboardText.isNullOrEmpty()) {
                        viewModel.setUrl(clipboardText)
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "Paste")
            }
            OutlinedButton(onClick = onDownloadsClick, modifier = Modifier.weight(1f)) {
                Text("Downloads")
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = { onContinueClick(urlState) },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        ) { Text(text = "Continue") }
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onSettingsClick, modifier = Modifier.fillMaxWidth()) {
            Text("Settings")
        }
    }
}