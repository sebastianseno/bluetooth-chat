package com.example.myapplication.presentation.screen.devicescan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.domain.ConnectionState
import com.example.myapplication.presentation.components.Device
import com.example.myapplication.presentation.components.button.BlueFloatingButton
import com.example.myapplication.presentation.components.button.FloatingButton
import com.example.myapplication.presentation.screen.viewmodel.BluetoothViewModel

@Composable
fun DeviceScanScreen(
    viewModel: BluetoothViewModel = hiltViewModel(),
    onNavigate: (address: String) -> Unit
) {

    val state = viewModel.state.collectAsState()

    val deviceAddress = remember {
        mutableStateOf("")
    }

    LaunchedEffect(state.value.connectionStatus) {
        if (state.value.connectionStatus == ConnectionState.CONNECTED && deviceAddress.value.isNotEmpty()) {
            onNavigate(deviceAddress.value)
        }
    }
    Box(
        Modifier
            .padding(18.dp)
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            state.value.scannedDevices.forEach {
                Device(
                    deviceName = it.name ?: it.address ?: "Tidak dikenal",
                ) {
                    deviceAddress.value = it.address ?: ""
                    viewModel.connectToDevice(it)
                }
            }
        }
        Column( modifier = Modifier.align(Alignment.BottomCenter), verticalArrangement = Arrangement.spacedBy(15.dp)) {
            BlueFloatingButton(
                label = "Start Chat"
            ) {
                onNavigate("null")
            }
            FloatingButton(
            ) {
                viewModel.startScan()
            }
        }

    }
}