package com.example.downloader.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.downloader.data.model.VideoFormat
import com.example.downloader.data.model.VideoInfo
import com.example.downloader.data.repository.DefaultVideoRepository
import com.example.downloader.downloader.DownloadManager
import com.example.downloader.downloader.FastMetadataFetcher
import com.example.downloader.downloader.FormatFetcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DetailsUiState(
    val loadingMetadata: Boolean = false,
    val loadingFormats: Boolean = false,
    val videoInfo: VideoInfo? = null,
    val formats: List<VideoFormat> = emptyList(),
    val selectedFormat: VideoFormat? = null,
    val error: String? = null
)

class DetailsViewModel(
    app: Application
) : AndroidViewModel(app) {

    private val repository = DefaultVideoRepository(
        metadataFetcher = FastMetadataFetcher(),
        formatFetcher = FormatFetcher(),
        downloadManager = DownloadManager(app)
    )

    private val _uiState = MutableStateFlow(DetailsUiState())
    val uiState: StateFlow<DetailsUiState> = _uiState.asStateFlow()

    fun loadMetadata(url: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loadingMetadata = true, error = null)
            repository.fetchMetadata(url)
                .onSuccess { info ->
                    _uiState.value = _uiState.value.copy(videoInfo = info, loadingMetadata = false)
                }
                .onFailure { throwable ->
                    _uiState.value = _uiState.value.copy(
                        loadingMetadata = false,
                        error = throwable.message ?: "Metadata failed"
                    )
                }
        }
    }

    fun loadFormats(url: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loadingFormats = true, error = null)
            repository.fetchFormats(url)
                .onSuccess { formats ->
                    _uiState.value = _uiState.value.copy(
                        formats = formats,
                        selectedFormat = formats.firstOrNull(),
                        loadingFormats = false
                    )
                }
                .onFailure { throwable ->
                    _uiState.value = _uiState.value.copy(
                        loadingFormats = false,
                        error = throwable.message ?: "Formats failed"
                    )
                }
        }
    }

    fun selectFormat(format: VideoFormat) {
        _uiState.value = _uiState.value.copy(selectedFormat = format)
    }

    fun download(url: String) {
        val info = _uiState.value.videoInfo ?: return
        val selected = _uiState.value.selectedFormat ?: return
        repository.enqueueDownload(
            url = url,
            formatId = selected.formatId,
            title = info.title,
            extension = selected.extension
        )
    }
}
