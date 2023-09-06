package com.example.myapplication.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.myapplication.extension.navigateTo
import com.example.myapplication.presentation.screen.chat.ChatScreen
import com.example.myapplication.presentation.screen.viewmodel.BluetoothViewModel
import com.example.myapplication.presentation.screen.devicescan.DeviceScanScreen

fun NavGraphBuilder.addNavigationGraph(
    navController: NavHostController,
    viewModel: BluetoothViewModel
) {

    composable(Route.DeviceScanScreen.route) {
        DeviceScanScreen(viewModel = viewModel) { deviceAddress ->
                navController.navigateTo(Route.ChatScreen.passData(deviceAddress), it)
        }
    }
    composable(
        route = Route.ChatScreen.route,
        arguments = listOf(
            navArgument("device_address") {
                type = NavType.StringType
            }
        )
    ) {
        ChatScreen(
            viewModel = viewModel,
            deviceAddress = it.arguments?.getString("device_address").orEmpty()
                .removeSurrounding(prefix = "{", suffix = "}")
        )
    }
}