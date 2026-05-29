package com.example.downloader.data.repository

import com.example.downloader.data.model.VideoFormat
import com.example.downloader.data.model.VideoInfo
import java.util.UUID

interface VideoRepository {
    suspend fun fetchMetadata(url: String): Result<VideoInfo>
    suspend fun fetchFormats(url: String): Result<List<VideoFormat>>
    fun enqueueDownload(url: String, formatId: String, title: String, extension: String): UUID
}
