package com.example.downloader.downloader

import android.util.Log
import com.example.downloader.data.model.VideoFormat
import com.example.downloader.data.model.VideoInfo
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import com.yausername.youtubedl_android.mapper.VideoInfo as YdlVideoInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.ServiceList
import java.util.concurrent.ConcurrentHashMap

private const val TAG = "VideoDetailsFetcher"
private const val CACHE_TTL_MS = 5 * 60 * 1_000L

data class VideoDetails(
    val info: VideoInfo,
    val formats: List<VideoFormat>
)

/**
 * Fetches video metadata and download formats.
 *
 * VideoFormat field types (from mapper source — do NOT guess):
 *   height: Int = 0       (non-null, 0 when absent)
 *   width:  Int = 0       (non-null, 0 when absent)
 *   abr:    Int = 0       (non-null, 0 when absent) — audio bitrate
 *   tbr:    Int = 0       (non-null, 0 when absent) — total bitrate
 *   vcodec: String? = null
 *   acodec: String? = null
 *   ext:    String? = null
 *   formatId: String? = null
 *   manifestUrl: String? = null  — non-null for HLS/DASH manifest streams
 *   url:    String? = null
 *
 * Strategy:
 *   fetchInfo()    → NewPipe first (fast), yt-dlp fallback
 *   fetchFormats() → yt-dlp only (NewPipe DASH is unreliable on Android)
 *   fetch()        → single yt-dlp call for both (efficient)
 *
 * Cache: prevents double yt-dlp invocation when ViewModel calls
 *        fetchInfo() + fetchFormats() in parallel.
 */
class VideoDetailsFetcher {

    private val cache = ConcurrentHashMap<String, Pair<Long, YdlVideoInfo>>()

    private fun getCached(url: String): YdlVideoInfo? {
        val (ts, info) = cache[url] ?: return null
        return if (System.currentTimeMillis() - ts <= CACHE_TTL_MS) info else null
    }

    private suspend fun getYdlInfo(url: String): YdlVideoInfo {
        getCached(url)?.let {
            Log.d(TAG, "Cache hit for $url")
            return it
        }
        val info = YoutubeDL.getInstance().getInfo(buildRequest(url))
        cache[url] = System.currentTimeMillis() to info
        return info
    }

    // ── Public API ────────────────────────────────────────────────────────────

    suspend fun fetchInfo(url: String): Result<VideoInfo> = withContext(Dispatchers.IO) {
        // NewPipe first: faster, no JS overhead
        runCatching {
            val extractor = ServiceList.YouTube.getStreamExtractor(url)
            extractor.fetchPage()
            VideoInfo(
                title       = extractor.name,
                thumbnail   = extractor.thumbnails.firstOrNull()?.url ?: "",
                duration    = extractor.length,
                channelName = extractor.uploaderName,
                views       = extractor.viewCount,
                uploadDate  = extractor.textualUploadDate ?: ""
            )
        }.onSuccess {
            Log.d(TAG, "fetchInfo: NewPipe OK")
        }.recoverCatching { npErr ->
            Log.w(TAG, "fetchInfo: NewPipe failed (${npErr.message}), falling back to yt-dlp")
            mapYdlInfo(getYdlInfo(url))
        }.onFailure {
            Log.e(TAG, "fetchInfo: yt-dlp also failed", it)
        }
    }

    suspend fun fetchFormats(url: String): Result<List<VideoFormat>> = withContext(Dispatchers.IO) {
        runCatching {
            parseFormats(getYdlInfo(url))
        }.onFailure {
            Log.e(TAG, "fetchFormats failed", it)
        }
    }

    suspend fun fetch(url: String): Result<VideoDetails> = withContext(Dispatchers.IO) {
        runCatching {
            val info = getYdlInfo(url)
            VideoDetails(info = mapYdlInfo(info), formats = parseFormats(info))
        }.onFailure {
            Log.e(TAG, "fetch failed", it)
        }
    }

    fun clearCache(url: String) { cache.remove(url) }

    // ── Mapping ───────────────────────────────────────────────────────────────

    private fun mapYdlInfo(info: YdlVideoInfo) = VideoInfo(
        title       = info.title     ?: "Unknown",
        thumbnail   = info.thumbnail ?: "",
        duration    = info.duration.toLong(),
        channelName = info.uploader  ?: "Unknown",
        views       = info.viewCount?.toLongOrNull() ?: 0L,
        uploadDate  = info.uploadDate ?: ""
    )

    // ── Format parsing ────────────────────────────────────────────────────────

    private fun parseFormats(info: YdlVideoInfo): List<VideoFormat> {
        val all = info.formats ?: emptyList()

        // Dump every raw format so we can debug filtering in Logcat
        Log.d(TAG, "══ RAW FORMATS (${all.size}) ══")
        all.forEachIndexed { i, f ->
            Log.d(TAG,
                "[$i] id=${f.formatId} ext=${f.ext} " +
                        "h=${f.height} w=${f.width} " +
                        "vcodec=${f.vcodec} acodec=${f.acodec} " +
                        "abr=${f.abr} tbr=${f.tbr} " +
                        "manifestUrl=${f.manifestUrl?.take(40)}"
            )
        }

        val formats = mutableListOf<VideoFormat>()

        // ── 1. DASH video-only ─────────────────────────────────────────────
        // Identified by: has real height, NO audio codec, NOT a manifest stream.
        // vcodec may be null on some clients — don't require it.
        // manifestUrl non-null → it's an HLS/DASH manifest entry, skip it.
        val videoOnly = all.filter { f ->
            f.height >= 144 &&
                    f.manifestUrl == null &&
                    f.ext != "mhtml" &&
                    (f.acodec == null || f.acodec == "none")
        }
        Log.d(TAG, "Video-only (${videoOnly.size}): ${videoOnly.map { "${it.height}p [${it.ext}]" }}")

        videoOnly
            .sortedByDescending { it.height }
            .distinctBy { it.height }
            .forEach { f ->
                formats += VideoFormat(
                    formatId  = "${f.formatId}+bestaudio",
                    quality   = "${f.height}p",
                    extension = f.ext ?: "mp4",
                    type      = "video"
                )
            }

        // ── 2. Mixed (video+audio muxed) ───────────────────────────────────
        // Only add for heights not already covered by video-only above.
        val coveredHeights = formats
            .map { it.quality.filter(Char::isDigit).toIntOrNull() ?: 0 }
            .toSet()

        val mixed = all.filter { f ->
            f.height >= 144 &&
                    f.height !in coveredHeights &&
                    f.manifestUrl == null &&
                    f.ext != "mhtml" &&
                    f.vcodec != null && f.vcodec != "none" &&
                    f.acodec != null && f.acodec != "none"
        }
        Log.d(TAG, "Mixed (${mixed.size}): ${mixed.map { "${it.height}p [${it.ext}]" }}")

        mixed
            .sortedByDescending { it.height }
            .distinctBy { it.height }
            .forEach { f ->
                formats += VideoFormat(
                    formatId  = f.formatId ?: "best",
                    quality   = "${f.height}p",
                    extension = f.ext ?: "mp4",
                    type      = "mixed"
                )
            }

        // ── 3. Audio-only ──────────────────────────────────────────────────
        val audioOnly = all.filter { f ->
            (f.vcodec == null || f.vcodec == "none") &&
                    f.acodec != null && f.acodec != "none" &&
                    f.manifestUrl == null &&
                    f.ext != "mhtml"
        }
        Log.d(TAG, "Audio-only (${audioOnly.size}): ${audioOnly.map { "${it.abr}kbps [${it.ext}]" }}")

        audioOnly
            .sortedByDescending { it.abr }
            .distinctBy { it.abr }
            .take(3)
            .forEach { f ->
                formats += VideoFormat(
                    formatId  = f.formatId ?: "bestaudio",
                    quality   = if (f.abr > 0) "Audio ${f.abr}kbps" else "Audio (High)",
                    extension = f.ext ?: "m4a",
                    type      = "audio"
                )
            }

        Log.d(TAG, "══ FINAL FORMATS (${formats.size}) ══")
        formats.forEach { Log.d(TAG, "  → ${it.quality} [${it.type}] id=${it.formatId}") }

        if (formats.isEmpty()) throw IllegalStateException(
            "No usable formats from ${all.size} raw entries. Check RAW FORMATS in Logcat."
        )

        return formats.sortedWith(
            compareByDescending<VideoFormat> { it.type != "audio" }
                .thenByDescending { it.quality.filter(Char::isDigit).toIntOrNull() ?: 0 }
        )
    }

    // ── Request ───────────────────────────────────────────────────────────────

    private fun buildRequest(url: String) = YoutubeDLRequest(url).apply {
        // android_web_no_sabr and web_embedded are currently the most reliable for DASH
        addOption("--extractor-args", "youtube:player_client=android_web_no_sabr,web_embedded")
        addOption("--no-playlist")
        addOption("--no-check-certificate")
        addOption("--geo-bypass")
        addOption("--all-formats")
        addOption("--check-formats")
    }
}