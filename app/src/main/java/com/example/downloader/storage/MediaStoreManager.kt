package com.example.downloader.storage

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import java.io.File
import java.io.FileInputStream

class MediaStoreManager(
    private val context: Context
) {
    private val fallbackMimeMap = mapOf(
        "mp3" to "audio/mpeg",
        "m4a" to "audio/mp4",
        "opus" to "audio/opus",
        "webm" to "video/webm",
        "mkv" to "video/x-matroska",
        "mp4" to "video/mp4"
    )

    fun saveDownload(source: File, displayName: String, extension: String): Result<Unit> = runCatching {
        val mimeType = MimeTypeMap.getSingleton()
            .getMimeTypeFromExtension(extension.lowercase())
            ?: fallbackMimeMap[extension.lowercase()]
            ?: "application/octet-stream"

        val (collection, relativePath) = when {
            mimeType.startsWith("audio") -> {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI to "${Environment.DIRECTORY_MUSIC}/YTDownloader"
            }
            mimeType.startsWith("video") -> {
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI to "${Environment.DIRECTORY_MOVIES}/YTDownloader"
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                MediaStore.Downloads.EXTERNAL_CONTENT_URI to "${Environment.DIRECTORY_DOWNLOADS}/YTDownloader"
            }
            else -> {
                // Fallback for older versions, though RELATIVE_PATH won't be used
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI to "${Environment.DIRECTORY_MOVIES}/YTDownloader"
            }
        }

        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "$displayName.$extension")
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
            }
        }

        val uri = context.contentResolver.insert(collection, values)
            ?: error("Unable to create MediaStore record")

        context.contentResolver.openOutputStream(uri)?.use { output ->
            FileInputStream(source).use { input -> input.copyTo(output) }
        } ?: error("Unable to open MediaStore output stream")
    }
}
