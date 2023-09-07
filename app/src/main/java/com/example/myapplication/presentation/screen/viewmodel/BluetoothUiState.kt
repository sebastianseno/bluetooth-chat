package com.example.myapplication.presentation.screen.viewmodel

import android.bluetooth.BluetoothDevice
import com.example.myapplication.domain.BluetoothDeviceDataClass
import com.example.myapplication.domain.ConnectionState
import com.example.myapplication.domain.MessageDataClass

data class BluetoothUiState(
    val scannedDevices: List<BluetoothDevice> = emptyList(),
    val pairedDevices: List<BluetoothDeviceDataClass> = emptyList(),
    val isConnected: Boolean = false,
    val isConnecting: Boolean = false,
    val selectedAddress: String = "",
    val chatMessages: List<MessageDataClass?> = emptyList(),
    val connectionStatus: ConnectionState = ConnectionState.DISCONNECTED
)
