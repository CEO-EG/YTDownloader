package com.example.downloader.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SettingsUiState(
    val downloadFolder: String = "Downloads/YTDownloader",
    val darkTheme: Boolean = true,
    val defaultQuality: String = "720p"
)

class SettingsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun setDefaultQuality(quality: String) {
        _uiState.value = _uiState.value.copy(defaultQuality = quality)
    }
}
