package com.example.downloader.downloader

import com.example.downloader.data.model.VideoInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.stream.StreamExtractor

class FastMetadataFetcher {
    suspend fun fetch(url: String): Result<VideoInfo> = withContext(Dispatchers.IO) {
        runCatching {
            val service = NewPipe.getService(ServiceList.YouTube.serviceId)
            val extractor = service.streamExtractorFactory.getExtractor(url) as StreamExtractor
            extractor.fetchPage()

            VideoInfo(
                title = callString(extractor, "getName", "getTitle") ?: "",
                thumbnail = callString(extractor, "getThumbnailUrl") ?: "",
                duration = callLong(extractor, "getLengthInSeconds", "getDuration") ?: 0L,
                channelName = callString(extractor, "getUploaderName", "getChannelName") ?: "",
                views = callLong(extractor, "getViewCount") ?: 0L,
                uploadDate = callObject(extractor, "getUploadDate")?.toString().orEmpty()
            )
        }
    }

    private fun callString(target: Any, vararg methodNames: String): String? =
        methodNames.firstNotNullOfOrNull { method ->
            callObject(target, method)?.toString()
        }

    private fun callLong(target: Any, vararg methodNames: String): Long? =
        methodNames.firstNotNullOfOrNull { method ->
            (callObject(target, method) as? Number)?.toLong()
        }

    private fun callObject(target: Any, methodName: String): Any? =
        runCatching { target.javaClass.getMethod(methodName).invoke(target) }.getOrNull()
}
