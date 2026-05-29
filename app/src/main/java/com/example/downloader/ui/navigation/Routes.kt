package com.example.downloader.ui.navigation

sealed class Routes(val route: String) {

    data object Home : Routes("home")

    data object Details : Routes("details")

    data object Downloads : Routes("downloads")

    data object Settings : Routes("settings")
}