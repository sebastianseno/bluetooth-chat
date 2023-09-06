package com.example.myapplication.presentation.screen

import com.example.myapplication.domain.BluetoothDeviceDataClass
import com.example.myapplication.domain.ConnectionState

data class BluetoothUiState(
    val scannedDevices: List<BluetoothDeviceDataClass> = emptyList(),
    val pairedDevices: List<BluetoothDeviceDataClass> = emptyList(),
    val isConnected: Boolean = false,
    val isConnecting: Boolean = false,
    val selectedAddress: String = "",
    val connectionStatus: ConnectionState = ConnectionState.DISCONNECTED
)
