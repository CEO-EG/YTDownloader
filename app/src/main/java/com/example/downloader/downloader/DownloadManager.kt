package com.example.downloader.downloader

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.downloader.workers.DownloadWorker
import java.util.UUID

class DownloadManager(
    private val context: Context
) {
    fun enqueue(url: String, formatId: String, title: String, extension: String): UUID {
        val request = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setInputData(
                Data.Builder()
                    .putString(DownloadWorker.KEY_URL, url)
                    .putString(DownloadWorker.KEY_FORMAT_ID, formatId)
                    .putString(DownloadWorker.KEY_TITLE, title)
                    .putString(DownloadWorker.KEY_EXTENSION, extension)
                    .build()
            )
            .addTag(DownloadWorker.TAG)
            .build()

        WorkManager.getInstance(context).enqueue(request)
        return request.id
    }
}
