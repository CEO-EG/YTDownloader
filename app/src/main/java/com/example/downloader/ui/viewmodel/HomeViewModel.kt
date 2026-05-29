package com.example.downloader.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewModel : ViewModel() {
    private val _url = MutableStateFlow("")
    val url: StateFlow<String> = _url.asStateFlow()

    fun setUrl(value: String) {
        _url.value = value
    }
}
