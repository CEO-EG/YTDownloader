package com.example.downloader.core.binary


import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

class BinaryInstaller(
    private val context: Context
) {

    private val client = OkHttpClient()

    companion object {

        private const val YT_DLP_URL =
            "https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp"

        private const val BINARY_NAME =  "yt-dlp"
    }

    suspend fun installYtDlp(): Result<File> {

        return withContext(Dispatchers.IO) {

            try {

                val toolsDir = File(
                    context.filesDir,
                    "tools"
                )

                if (!toolsDir.exists()) {
                    toolsDir.mkdirs()
                }

                val binaryFile = File(
                    toolsDir,
                    BINARY_NAME
                )

                if (binaryFile.exists()) {

                    return@withContext Result.success(binaryFile)
                }

                val request = Request.Builder()
                    .url(YT_DLP_URL)
                    .build()

                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {

                    return@withContext Result.failure(
                        Exception("Download failed")
                    )
                }

                val body = response.body

                if (body == null) {

                    return@withContext Result.failure(
                        Exception("Empty response body")
                    )
                }

                binaryFile.outputStream().use { output ->

                    body.byteStream().copyTo(output)
                }

                binaryFile.setExecutable(true)

                Result.success(binaryFile)

            } catch (e: Exception) {

                Result.failure(e)
            }
        }
    }
}