package com.example.downloader.ffmpeg

import java.io.File

class FFmpegManager {
    fun mergeIfNeeded(videoFile: File, audioFile: File?): Result<File> = runCatching {
        audioFile ?: return@runCatching videoFile
        videoFile
    }
}
