package com.example.downloader.data.repository

import com.example.downloader.data.model.VideoFormat
import com.example.downloader.data.model.VideoInfo
import com.example.downloader.downloader.VideoDetails
import java.util.UUID

interface VideoRepository {
    suspend fun fetchVideoDetails(url: String): Result<VideoDetails>
    suspend fun fetchVideoInfo(url: String): Result<VideoInfo>
    suspend fun fetchVideoFormats(url: String): Result<List<VideoFormat>>
    fun enqueueDownload(url: String, formatId: String, title: String, extension: String): UUID
}
