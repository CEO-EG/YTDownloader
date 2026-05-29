package com.example.downloader.storage

import android.content.ContentValues
import android.content.Context
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
        "opus" to "audio/ogg",
        "webm" to "video/webm",
        "mkv" to "video/x-matroska",
        "mp4" to "video/mp4"
    )

    fun saveDownload(source: File, displayName: String, extension: String): Result<Unit> = runCatching {
        val relativePath = "${Environment.DIRECTORY_DOWNLOADS}/YTDownloader"
        val mimeType = MimeTypeMap.getSingleton()
            .getMimeTypeFromExtension(extension.lowercase())
            ?: fallbackMimeMap[extension.lowercase()]
            ?: "application/octet-stream"
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "$displayName.$extension")
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
        }

        val collection = if (mimeType.startsWith("audio")) {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }

        val uri = context.contentResolver.insert(collection, values)
            ?: error("Unable to create MediaStore record")

        context.contentResolver.openOutputStream(uri)?.use { output ->
            FileInputStream(source).use { input -> input.copyTo(output) }
        } ?: error("Unable to open MediaStore output stream")
    }
}
