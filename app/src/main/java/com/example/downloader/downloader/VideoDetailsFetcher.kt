package com.example.downloader.downloader

import com.example.downloader.data.model.VideoFormat
import com.example.downloader.data.model.VideoInfo
import com.yausername.youtubedl_android.YoutubeDL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.services.youtube.YoutubeService

data class VideoDetails(
    val info: VideoInfo,
    val formats: List<VideoFormat>
)

class VideoDetailsFetcher {
    
    suspend fun fetchInfo(url: String): Result<VideoInfo> = withContext(Dispatchers.IO) {
        val newPipeResult = runCatching {
            val service = ServiceList.YouTube
            val extractor = service.getStreamExtractor(url)
            extractor.fetchPage()
            
            VideoInfo(
                title = extractor.name,
                thumbnail = extractor.thumbnails.firstOrNull()?.url ?: "",
                duration = extractor.length,
                channelName = extractor.uploaderName,
                views = extractor.viewCount,
                uploadDate = extractor.textualUploadDate ?: ""
            )
        }

        if (newPipeResult.isSuccess) return@withContext newPipeResult

        // Fallback to YoutubeDL if NewPipe fails
        runCatching {
            val info = YoutubeDL.getInstance().getInfo(url)
            VideoInfo(
                title = info.title ?: "Unknown",
                thumbnail = info.thumbnail ?: "",
                duration = info.duration.toLong(),
                channelName = info.uploader ?: "Unknown",
                views = info.viewCount?.toLongOrNull() ?: 0L,
                uploadDate = info.uploadDate ?: ""
            )
        }
    }

    suspend fun fetchFormats(url: String): Result<List<VideoFormat>> = withContext(Dispatchers.IO) {
        runCatching {
            val info = YoutubeDL.getInstance().getInfo(url)
            val formats = mutableListOf<VideoFormat>()
            
            val allFormats = info.formats ?: emptyList()
            
            // 720p Mixed (Look for any format containing 720p with both codecs)
            allFormats.firstOrNull { 
                (it.formatNote?.contains("720p") == true || it.height == 720) && 
                it.vcodec != "none" && it.acodec != "none" 
            }?.let {
                formats.add(VideoFormat(it.formatId ?: "", "720p (Mixed)", it.ext ?: "mp4", "mixed"))
            }

            // 360p Mixed
            allFormats.firstOrNull { 
                (it.formatNote?.contains("360p") == true || it.height == 360) && 
                it.vcodec != "none" && it.acodec != "none" 
            }?.let {
                formats.add(VideoFormat(it.formatId ?: "", "360p (Mixed)", it.ext ?: "mp4", "mixed"))
            }
            
            // Fallback: If no mixed 720p/360p found, try to find just video 720p/360p
            if (formats.none { it.quality.contains("720p") }) {
                allFormats.firstOrNull { it.formatNote?.contains("720p") == true || it.height == 720 }?.let {
                    formats.add(VideoFormat(it.formatId ?: "", "720p (Video only)", it.ext ?: "mp4", "video"))
                }
            }
            if (formats.none { it.quality.contains("360p") }) {
                allFormats.firstOrNull { it.formatNote?.contains("360p") == true || it.height == 360 }?.let {
                    formats.add(VideoFormat(it.formatId ?: "", "360p (Video only)", it.ext ?: "mp4", "video"))
                }
            }

            // Two audio options
            allFormats.filter { it.vcodec == "none" }
                .sortedByDescending { it.abr }
                .take(2)
                .forEach { 
                    formats.add(VideoFormat(
                        it.formatId ?: "", 
                        "Audio ${it.abr.toInt()}kbps",
                        it.ext ?: "m4a", 
                        "audio"
                    ))
                }

            formats
        }
    }

    // Keep original fetch for backward compatibility if needed, but updated to use both
    suspend fun fetch(url: String): Result<VideoDetails> = withContext(Dispatchers.IO) {
        runCatching {
            val info = fetchInfo(url).getOrThrow()
            val formats = fetchFormats(url).getOrThrow()
            VideoDetails(info, formats)
        }
    }
}
