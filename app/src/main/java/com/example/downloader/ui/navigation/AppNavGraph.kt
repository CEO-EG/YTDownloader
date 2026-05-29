package com.example.downloader.ui.navigation


import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.downloader.ui.screens.details.DetailsScreen
import com.example.downloader.ui.screens.downloads.DownloadsScreen
import com.example.downloader.ui.screens.home.HomeScreen
import com.example.downloader.ui.screens.settings.SettingsScreen
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun AppNavGraph() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.Home.route
    ) {

        composable(
            route = Routes.Home.route
        ) {

            HomeScreen(
                onContinueClick = { url ->
                    val encodedUrl = URLEncoder.encode(url, StandardCharsets.UTF_8.toString())
                    navController.navigate(
                        "${Routes.Details.route}?url=$encodedUrl"
                    )
                },
                onDownloadsClick = { navController.navigate(Routes.Downloads.route) },
                onSettingsClick = { navController.navigate(Routes.Settings.route) }
            )
        }

        composable(
            route = "${Routes.Details.route}?url={url}",

            arguments = listOf(
                navArgument("url") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->

            val url = backStackEntry
                .arguments
                ?.getString("url")
                ?: ""

            DetailsScreen(
                url = url
            )
        }

        composable(route = Routes.Downloads.route) {
            DownloadsScreen()
        }

        composable(route = Routes.Settings.route) {
            SettingsScreen()
        }
    }
}