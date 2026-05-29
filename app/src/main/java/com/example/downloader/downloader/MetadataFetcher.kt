package com.example.downloader.downloader

import android.content.Context
import com.example.downloader.data.model.VideoInfo
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class MetadataFetcher(
    private val context: Context
) {

    suspend fun fetch(
        url: String
    ): Result<VideoInfo> {

        return withContext(Dispatchers.IO) {

            try {

                val request = YoutubeDLRequest(url)

                request.addOption("--dump-json")

                request.addOption("--no-playlist")

                request.addOption("--skip-download")

                request.addOption("--no-check-certificates")

                request.addOption("--flat-playlist")

                request.addOption("--no-warnings")

                request.addOption("--extractor-args", "youtube:player_client=android")

                val response = YoutubeDL
                    .getInstance()
                    .execute(request)

                val json = Json {
                    ignoreUnknownKeys = true
                }

                val videoInfo = json.decodeFromString<VideoInfo>(
                    response.out
                )

                Result.success(videoInfo)

            } catch (e: Exception) {

                Result.failure(e)
            }
        }
    }
}