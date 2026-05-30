package com.example.downloader

import android.app.Application
import android.util.Log
import com.yausername.youtubedl_android.YoutubeDL
import okhttp3.OkHttpClient
import com.example.downloader.downloader.NewPipeDownloader
import org.schabi.newpipe.extractor.NewPipe

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        
        NewPipe.init(NewPipeDownloader(OkHttpClient()))

        try {

            YoutubeDL.getInstance().init(this)

            Log.d(
                "YT_DLP",
                "yt-dlp initialized"
            )

        } catch (e: Exception) {

            Log.e(
                "YT_DLP",
                "Initialization failed",
                e
            )
        }
    }
}