package com.example.downloader

import android.app.Application
import android.util.Log
import com.yausername.youtubedl_android.YoutubeDL

class App : Application() {

    override fun onCreate() {

        super.onCreate()

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