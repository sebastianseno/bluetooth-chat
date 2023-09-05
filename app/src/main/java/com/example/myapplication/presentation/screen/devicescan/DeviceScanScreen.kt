package com.example.myapplication.presentation.screen.devicescan

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.presentation.components.Device
import com.example.myapplication.presentation.components.FloatingButton
import com.example.myapplication.presentation.screen.BluetoothViewModel

@Composable
fun DeviceScanScreen() {
    val viewModel = hiltViewModel<BluetoothViewModel>()
    val state = viewModel.state.collectAsState()

    Box(Modifier.padding(18.dp)) {
        Column {
            Log.d("senoo",  state.value.scannedDevices.toString())
            state.value.scannedDevices.forEach {
                Device(deviceName = it.name ?: "Tidak dikenal", )
            }
        }
        FloatingButton(
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            viewModel.startScan()
        }

    }
}