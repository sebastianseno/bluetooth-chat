package com.example.myapplication.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.myapplication.presentation.screen.chat.ChatScreen
import com.example.myapplication.presentation.screen.devicescan.DeviceScanScreen

fun NavGraphBuilder.addNavigationGraph(
    navController: NavController
) {
//    composable(Route.DeviceScanScreen.route) {
//        DeviceScanScreen(navController = navController, navBackStackEntry = it)
//    }
    composable(
        route = Route.ChatScreen.route,
        arguments = listOf(
            navArgument("device_address") {
                type = NavType.StringType
            }
        )
    ) {
        ChatScreen(
            deviceAddress = it.arguments?.getString("device_address").orEmpty()
                .removeSurrounding(prefix = "{", suffix = "}")
        )
    }
}