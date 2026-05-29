package com.example.downloader.ui.navigation

sealed class Routes(val route: String) {

    data object Home : Routes("home")

    data object Details : Routes("details")

}