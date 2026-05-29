package com.example.downloader.ui.screens.home

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import android.widget.Toast
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.example.downloader.core.binary.BinaryInstaller
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import kotlin.math.log

@Composable
fun HomeScreen(
    onContinueClick: (String) -> Unit
) {

    val clipboardManager = LocalClipboardManager.current

    val urlState = remember {
        mutableStateOf("")
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),

        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "YouTube Downloader",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = urlState.value,
            onValueChange = {
                urlState.value = it
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text(text = "Paste YouTube URL")
            },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = {

                val clipboardText = clipboardManager
                    .getText()
                    ?.text
                    ?.toString()

                if (!clipboardText.isNullOrEmpty()) {
                    urlState.value = clipboardText
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {

            Text(text = "Paste")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                Log.d(
                    "HomeScreen",
                    "Continue clicked with URL: ${urlState.value}"
                )

                onContinueClick(
                    urlState.value
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {

            Text(text = "Continue")
        }
    }
}