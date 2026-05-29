package com.example.downloader.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.downloader.workers.DownloadWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class DownloadsUiState(
    val active: List<WorkInfo> = emptyList(),
    val completed: List<WorkInfo> = emptyList(),
    val failed: List<WorkInfo> = emptyList()
)

class DownloadsViewModel(app: Application) : AndroidViewModel(app) {
    private val workManager = WorkManager.getInstance(app)
    private val _uiState = MutableStateFlow(DownloadsUiState())
    val uiState: StateFlow<DownloadsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            workManager.getWorkInfosByTagLiveData(DownloadWorker.TAG).asFlow().collect { items ->
                _uiState.update {
                    it.copy(
                        active = items.filter { info -> !info.state.isFinished },
                        completed = items.filter { info -> info.state == WorkInfo.State.SUCCEEDED },
                        failed = items.filter { info -> info.state == WorkInfo.State.FAILED }
                    )
                }
            }
        }
    }

    fun retry(id: UUID) {
        workManager.getWorkInfoByIdLiveData(id).value?.let { info ->
            if (info.outputData.hasRetryInput()) {
                workManager.enqueue(info.toWorkRequest())
            }
        }
    }
}

private fun WorkInfo.toWorkRequest() = androidx.work.OneTimeWorkRequestBuilder<com.example.downloader.workers.DownloadWorker>()
    .setInputData(this.outputData)
    .addTag(DownloadWorker.TAG)
    .build()

private fun androidx.work.Data.hasRetryInput(): Boolean =
    getString(DownloadWorker.KEY_URL) != null &&
        getString(DownloadWorker.KEY_FORMAT_ID) != null
