package com.example.downloader.data.model

data class VideoInfo(
    val title: String,
    val thumbnail: String,
    val duration: Long,
    val channelName: String,
    val views: Long,
    val uploadDate: String
)