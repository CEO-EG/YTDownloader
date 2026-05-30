package com.example.downloader.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.downloader.data.model.VideoFormat
import com.example.downloader.data.model.VideoInfo
import com.example.downloader.data.repository.DefaultVideoRepository
import com.example.downloader.downloader.DownloadManager
import com.example.downloader.downloader.VideoDetailsFetcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DetailsUiState(
    val isLoading: Boolean = false,
    val isFormatsLoading: Boolean = false,
    val videoInfo: VideoInfo? = null,
    val formats: List<VideoFormat> = emptyList(),
    val selectedFormat: VideoFormat? = null,
    val error: String? = null
)

class DetailsViewModel(
    app: Application
) : AndroidViewModel(app) {

    private val repository = DefaultVideoRepository(
        detailsFetcher = VideoDetailsFetcher(),
        downloadManager = DownloadManager(app)
    )

    private val _uiState = MutableStateFlow(DetailsUiState())
    val uiState: StateFlow<DetailsUiState> = _uiState.asStateFlow()

    fun loadData(url: String) {
        _uiState.update { it.copy(isLoading = true, isFormatsLoading = true, error = null, formats = emptyList(), videoInfo = null) }
        
        // Start fetching info (fast)
        viewModelScope.launch {
            repository.fetchVideoInfo(url)
                .onSuccess { info ->
                    _uiState.update { it.copy(
                        videoInfo = info,
                        isLoading = false
                    ) }
                }
                .onFailure { throwable ->
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = throwable.message ?: "Failed to load video info"
                    ) }
                }
        }

        // Start fetching formats IMMEDIATELY in parallel
        viewModelScope.launch {
            repository.fetchVideoFormats(url)
                .onSuccess { formats ->
                    _uiState.update { it.copy(
                        formats = formats,
                        selectedFormat = formats.firstOrNull(),
                        isFormatsLoading = false
                    ) }
                }
                .onFailure {
                    _uiState.update { it.copy(isFormatsLoading = false) }
                }
        }
    }

    fun selectFormat(format: VideoFormat) {
        _uiState.update { it.copy(selectedFormat = format) }
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
