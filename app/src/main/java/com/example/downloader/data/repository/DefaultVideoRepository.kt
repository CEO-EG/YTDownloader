package com.example.downloader.data.repository

import com.example.downloader.data.model.VideoFormat
import com.example.downloader.data.model.VideoInfo
import com.example.downloader.downloader.DownloadManager
import com.example.downloader.downloader.VideoDetails
import com.example.downloader.downloader.VideoDetailsFetcher
import java.util.UUID

class DefaultVideoRepository(
    private val detailsFetcher: VideoDetailsFetcher,
    private val downloadManager: DownloadManager
) : VideoRepository {
    override suspend fun fetchVideoDetails(url: String): Result<VideoDetails> = 
        detailsFetcher.fetch(url)

    override suspend fun fetchVideoInfo(url: String): Result<VideoInfo> =
        detailsFetcher.fetchInfo(url)

    override suspend fun fetchVideoFormats(url: String): Result<List<VideoFormat>> =
        detailsFetcher.fetchFormats(url)

    override fun enqueueDownload(url: String, formatId: String, title: String, extension: String): UUID =
        downloadManager.enqueue(url = url, formatId = formatId, title = title, extension = extension)
}
