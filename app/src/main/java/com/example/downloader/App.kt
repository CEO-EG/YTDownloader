package com.example.downloader

import android.app.Application
import android.util.Log
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.ffmpeg.FFmpeg
import com.yausername.aria2c.Aria2c
import okhttp3.OkHttpClient
import com.example.downloader.downloader.NewPipeDownloader
import org.schabi.newpipe.extractor.NewPipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        
        NewPipe.init(NewPipeDownloader(OkHttpClient()))

        try {
            YoutubeDL.getInstance().init(this)
            FFmpeg.getInstance().init(this)
            Aria2c.getInstance().init(this)

            Log.d(
                "YT_DLP",
                "yt-dlp, ffmpeg and aria2c initialized"
            )
            
            // Update yt-dlp binary on startup
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    YoutubeDL.getInstance().updateYoutubeDL(this@App)
                    Log.d("YT_DLP", "yt-dlp updated successfully")
                } catch (e: Exception) {
                    Log.e("YT_DLP", "yt-dlp update failed", e)
                }
            }

        } catch (e: Exception) {

            Log.e(
                "YT_DLP",
                "Initialization failed",
                e
            )
        }
    }
}