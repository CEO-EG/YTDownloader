package com.example.downloader.workers

import android.content.Context
import android.util.Log
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
        
        setProgress(Data.Builder()
            .putInt(KEY_PROGRESS, 0)
            .putString(KEY_TITLE, title)
            .build())

        val finalFormat = formatId

        val request = YoutubeDLRequest(url).apply {
            addOption("-f", finalFormat)
            addOption("-o", outputTemplate)
            addOption("--no-playlist")
            addOption("--extractor-args", "youtube:player_client=android_web_no_sabr,web_embedded")
            addOption("--no-check-certificate")
            addOption("--geo-bypass")
            addOption("--check-formats")
            addOption("--downloader", "aria2c")
            addOption("--external-downloader-args", "aria2c:--summary-interval=1")
        }

        try {
            YoutubeDL.getInstance().execute(request) { progress, _, line ->
                Log.d(TAG, "Progress: $progress, Line: $line")
                if (progress > 0f) {
                    setProgressAsync(Data.Builder()
                        .putInt(KEY_PROGRESS, progress.toInt())
                        .putString(KEY_TITLE, title)
                        .build())
                }
            }
            
            setProgress(Data.Builder()
                .putInt(KEY_PROGRESS, 95)
                .putString(KEY_TITLE, title)
                .build())

            val latestFile = downloadDir.listFiles()?.firstOrNull()
                ?: error("No downloaded file found")
            val finalFile = FFmpegManager().mergeIfNeeded(latestFile, null).getOrThrow()
            MediaStoreManager(applicationContext).saveDownload(finalFile, title, extension).getOrThrow()
            
            setProgress(Data.Builder()
                .putInt(KEY_PROGRESS, 100)
                .putString(KEY_TITLE, title)
                .build())
                
            return Result.success(Data.Builder()
                .putString(KEY_TITLE, title)
                .build())
        } catch (error: Exception) {
            Log.e(TAG, "Download failed", error)
            return Result.failure(
                Data.Builder()
                    .putString(KEY_URL, url)
                    .putString(KEY_FORMAT_ID, formatId)
                    .putString(KEY_TITLE, title)
                    .putString(KEY_EXTENSION, extension)
                    .build()
            )
        } finally {
            downloadDir.deleteRecursively()
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
