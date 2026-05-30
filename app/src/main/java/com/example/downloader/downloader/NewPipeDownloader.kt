package com.example.downloader.downloader

import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Request
import org.schabi.newpipe.extractor.downloader.Response
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class NewPipeDownloader(private val client: OkHttpClient) : Downloader() {
    override fun execute(request: Request): Response {
        val method = request.httpMethod()
        val url = request.url()
        val headers = request.headers()
        val dataToSend = request.dataToSend()

        val okHttpRequest = okhttp3.Request.Builder()
            .url(url)
            .apply {
                var hasUserAgent = false
                headers.forEach { (key, values) ->
                    if (key.equals("User-Agent", ignoreCase = true)) {
                        hasUserAgent = true
                    }
                    values.forEach { value -> addHeader(key, value) }
                }
                if (!hasUserAgent) {
                    addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                }
                if (headers["Accept-Language"] == null) {
                    addHeader("Accept-Language", "en-US,en;q=0.5")
                }

                if (method == "POST") {
                    post(dataToSend?.toRequestBody() ?: ByteArray(0).toRequestBody())
                } else {
                    method(method, null)
                }
            }
            .build()

        val response = client.newCall(okHttpRequest).execute()
        val responseBody = response.body?.string()

        if (!response.isSuccessful) {
            throw IOException("Unexpected code $response")
        }

        return Response(
            response.code,
            response.message,
            response.headers.toMultimap(),
            responseBody,
            response.request.url.toString()
        )
    }
}
