package com.example.downloader.data.repository

import com.example.downloader.data.model.VideoFormat
import com.example.downloader.data.model.VideoInfo
import com.example.downloader.downloader.DownloadManager
import com.example.downloader.downloader.FastMetadataFetcher
import com.example.downloader.downloader.FormatFetcher
import java.util.UUID

class DefaultVideoRepository(
    private val metadataFetcher: FastMetadataFetcher,
    private val formatFetcher: FormatFetcher,
    private val downloadManager: DownloadManager
) : VideoRepository {
    override suspend fun fetchMetadata(url: String): Result<VideoInfo> = metadataFetcher.fetch(url)

    override suspend fun fetchFormats(url: String): Result<List<VideoFormat>> = formatFetcher.fetch(url)

    override fun enqueueDownload(url: String, formatId: String, title: String, extension: String): UUID =
        downloadManager.enqueue(url = url, formatId = formatId, title = title, extension = extension)
}
