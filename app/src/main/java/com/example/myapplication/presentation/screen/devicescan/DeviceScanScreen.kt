package com.example.myapplication.presentation.screen.devicescan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.domain.ConnectionState
import com.example.myapplication.presentation.components.Device
import com.example.myapplication.presentation.components.button.BlueFloatingButton
import com.example.myapplication.presentation.components.button.FloatingButton
import com.example.myapplication.presentation.screen.viewmodel.BluetoothViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
fun DeviceScanScreen(
    viewModel: BluetoothViewModel = hiltViewModel(),
    onNavigate: (address: String) -> Unit
) {

    val state = viewModel.state.collectAsState()
    var deviceAddress by rememberSaveable { mutableStateOf("") }
    val systemUiController = rememberSystemUiController()

    LaunchedEffect(state.value.connectionStatus, deviceAddress) {
        if (state.value.connectionStatus == ConnectionState.CONNECTED) {
            onNavigate(deviceAddress)
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
                    onNavigate("null")
                }
            }
            FloatingButton {
                viewModel.startScan()
            }
        }

    }
}