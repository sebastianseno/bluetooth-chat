package com.example.myapplication.presentation.screen.devicescan

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.example.myapplication.domain.ConnectionState
import com.example.myapplication.extension.navigateTo
import com.example.myapplication.navigation.Route
import com.example.myapplication.presentation.components.Device
import com.example.myapplication.presentation.components.button.BlueFloatingButton
import com.example.myapplication.presentation.components.button.FloatingButton
import com.example.myapplication.presentation.screen.viewmodel.BluetoothViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
fun DeviceScanScreen(
    viewModel: BluetoothViewModel = hiltViewModel(),
    navController: NavController,
    navBackStackEntry: NavBackStackEntry
) {

    val state = viewModel.state.collectAsState()
    var deviceAddress by rememberSaveable { mutableStateOf("") }
    val systemUiController = rememberSystemUiController()

    LaunchedEffect(state.value.connectionStatus, deviceAddress) {
        if (state.value.connectionStatus == ConnectionState.CONNECTED) {
            navController.navigateTo(Route.ChatScreen.passData(deviceAddress), navBackStackEntry)
        }
    }
    Box(
        Modifier
            .padding(18.dp)
            .fillMaxSize()
    ) {
        systemUiController.setStatusBarColor(
            color = MaterialTheme.colors.primary
        )
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            state.value.scannedDevices.forEach {
                Device(
                    deviceName = it.name ?: it.address ?: "Tidak dikenal",
                ) {
                    deviceAddress = it.address ?: "empty"
                    viewModel.connectToDevice(it)
                }
            }
        }
        Column(
            modifier = Modifier.align(Alignment.BottomCenter),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            if (state.value.pairedDevices.isNotEmpty()) {
                BlueFloatingButton(
                    label = "Start Chat"
                ) {
                    navController.navigateTo(Route.ChatScreen.passData("null"), navBackStackEntry)
                }
            }
            FloatingButton {
                viewModel.startScan()
            }
        }

    }
}