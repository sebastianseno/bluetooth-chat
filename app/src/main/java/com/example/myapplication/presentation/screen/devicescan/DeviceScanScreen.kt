package com.example.myapplication.presentation.screen.devicescan

import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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
//    navController: NavController,
//    navBackStackEntry: NavBackStackEntry,
    onClick: () -> Unit
) {

    val state = viewModel.state.collectAsState()
    var deviceAddress by rememberSaveable { mutableStateOf("") }
    val systemUiController = rememberSystemUiController()

    LaunchedEffect(state.value.connectionStatus, deviceAddress) {
        if (state.value.connectionStatus == ConnectionState.CONNECTED && deviceAddress.isNotEmpty()) {
            onClick()
//            navController.navigateTo(Route.ChatScreen.passData(deviceAddress), navBackStackEntry)
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
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            state.value.scannedDevices.forEach {
                val name = it.name ?: it.address ?: "Tidak dikenal"
                Device(
                    deviceName = name,
                    isLoading = it.address == deviceAddress && state.value.connectionStatus == ConnectionState.CONNECTING
                ) {
                    viewModel.setCurrentUser(it)
                    deviceAddress = it.address ?: "empty"
                }
            }
        }
        Column(
            modifier = Modifier.align(Alignment.BottomCenter),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            FloatingButton {
                viewModel.startScan()
            }
        }

    }
}