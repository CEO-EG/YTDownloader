package com.example.downloader.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.example.downloader.ffmpeg.FFmpegManager
import com.example.downloader.storage.MediaStoreManager
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import java.io.File

class DownloadWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val url = inputData.getString(KEY_URL) ?: return Result.failure()
        val formatId = inputData.getString(KEY_FORMAT_ID) ?: return Result.failure()
        val title = inputData.getString(KEY_TITLE).orEmpty().ifBlank { "download" }
        val extension = inputData.getString(KEY_EXTENSION).orEmpty().ifBlank { "mp4" }

        val downloadDir = File(applicationContext.cacheDir, "active_downloads/$id").apply { mkdirs() }
        val outputTemplate = File(downloadDir, "media.%(ext)s").absolutePath
        val request = YoutubeDLRequest(url).apply {
            addOption("-f", formatId)
            addOption("-o", outputTemplate)
            addOption("--no-playlist")
            addOption("--newline")
        }

        return runCatching {
            YoutubeDL.getInstance().execute(request)
            setProgressAsync(Data.Builder().putInt(KEY_PROGRESS, 95).build())

            val latestFile = downloadDir.listFiles()?.firstOrNull()
                ?: error("No downloaded file found")
            val finalFile = FFmpegManager().mergeIfNeeded(latestFile, null).getOrThrow()
            MediaStoreManager(applicationContext).saveDownload(finalFile, title, extension).getOrThrow()
            setProgressAsync(Data.Builder().putInt(KEY_PROGRESS, 100).build())
            Result.success()
        }.getOrElse {
            Result.failure(
                Data.Builder()
                    .putString(KEY_URL, url)
                    .putString(KEY_FORMAT_ID, formatId)
                    .putString(KEY_TITLE, title)
                    .putString(KEY_EXTENSION, extension)
                    .build()
            )
        }
    }

    companion object {
        const val KEY_URL = "url"
        const val KEY_FORMAT_ID = "format_id"
        const val KEY_TITLE = "title"
        const val KEY_EXTENSION = "extension"
        const val KEY_PROGRESS = "progress"
        const val TAG = "download-work"
    }
}
