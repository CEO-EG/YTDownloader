package com.example.downloader.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VideoInfo(

    val title: String? = null,

    val duration: Int? = null,

    val thumbnail: String? = null,

    @SerialName("webpage_url")
    val webpageUrl: String? = null
)