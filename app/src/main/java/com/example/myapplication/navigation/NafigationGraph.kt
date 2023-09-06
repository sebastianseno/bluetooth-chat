package com.example.myapplication.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.myapplication.presentation.screen.chat.ChatScreen
import com.example.myapplication.presentation.screen.devicescan.BluetoothViewModel
import com.example.myapplication.presentation.screen.devicescan.DeviceScanScreen

fun NavGraphBuilder.addNavigationGraph(
    navController: NavHostController,
    viewModel: BluetoothViewModel
) {

    composable(Route.DeviceScanScreen.route) {
        DeviceScanScreen(viewModel = viewModel) { deviceAddress ->
            navController.navigate("${Route.ChatScreen.route}")
        }
    }
    composable(Route.ChatScreen.route) {
        ChatScreen()
    }
}