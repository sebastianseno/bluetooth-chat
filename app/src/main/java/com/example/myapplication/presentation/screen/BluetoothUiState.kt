package com.example.myapplication.presentation.screen

import com.example.myapplication.domain.BluetoothDeviceDataClass

data class BluetoothUiState(
    val scannedDevices: List<BluetoothDeviceDataClass> = emptyList(),
    val pairedDevices: List<BluetoothDeviceDataClass> = emptyList(),
    val isConnected: Boolean = false,
    val isConnecting: Boolean = false,
    val errorMessage: String? = null,
)
