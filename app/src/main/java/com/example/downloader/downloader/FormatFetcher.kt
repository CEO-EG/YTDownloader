package com.example.downloader.downloader

import com.example.downloader.data.model.VideoFormat
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FormatFetcher {
    suspend fun fetch(url: String): Result<List<VideoFormat>> = withContext(Dispatchers.IO) {
        runCatching {
            val request = YoutubeDLRequest(url).apply {
                addOption("-F")
                addOption("--no-playlist")
            }

            private val qualityRegex = Regex("\\b(\\d+p|\\d+x\\d+)\\b")
            val output = YoutubeDL.getInstance().execute(request).out
            parse(output)
        }
    }

    private fun parse(raw: String): List<VideoFormat> = raw.lineSequence()
        .map { it.trim() }
        .filter { it.isNotBlank() && it.firstOrNull()?.isDigit() == true }
        .mapNotNull { line ->
            val parts = line.split(Regex("\\s+"))
            if (parts.size < 3) return@mapNotNull null
            val formatId = parts[0]
            val extension = parts[1]
            val quality = qualityRegex.find(line)?.value ?: "unknown"
            val lower = line.lowercase()
            val type = when {
                "audio only" in lower -> "audio"
                "video only" in lower -> "video"
                else -> "muxed"
            }
            VideoFormat(formatId = formatId, quality = quality, extension = extension, type = type)
        }
        .toList()
}
