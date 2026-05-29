package com.example.downloader.ui.screens.details

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.example.downloader.data.model.VideoInfo
import com.example.downloader.downloader.MetadataFetcher


@Composable
fun DetailsScreen(
    url: String
) {

    val context = LocalContext.current

    val metadataFetcher = remember {
        MetadataFetcher(context)
    }

    val videoInfoState = remember {
        mutableStateOf<VideoInfo?>(null)
    }

    val loadingState = remember {
        mutableStateOf(true)
    }

    val errorState = remember {
        mutableStateOf<String?>(null)
    }

    LaunchedEffect(Unit) {

        val result = metadataFetcher.fetch(url)

        result.onSuccess {

            videoInfoState.value = it
            Log.d("DetailsScreen", "Fetched video info: $it")

            loadingState.value = false

        }.onFailure {

            errorState.value = it.message

            loadingState.value = false
        }
    }

    if (loadingState.value) {

        Column(
            modifier = Modifier.fillMaxSize(),

            verticalArrangement = Arrangement.Center,

            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            CircularProgressIndicator()
        }

        return
    }

    if (errorState.value != null) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),

            verticalArrangement = Arrangement.Center,

            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = errorState.value ?: "Unknown error"
            )
        }

        return
    }

    val videoInfo = videoInfoState.value ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        AsyncImage(
            model = videoInfo.thumbnail,
            contentDescription = null,

            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = videoInfo.title ?: "No Title",

            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Duration: ${videoInfo.duration ?: 0} sec"
        )
    }
}