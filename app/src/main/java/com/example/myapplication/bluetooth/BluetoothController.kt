package com.example.myapplication.bluetooth

import com.example.myapplication.domain.BluetoothDeviceDataClass
import com.example.myapplication.domain.ConnectionResult
import com.example.myapplication.domain.MessageDataClass
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface BluetoothController {
    val isConnected: StateFlow<Boolean>
    val scannedDevices: StateFlow<List<BluetoothDeviceDataClass>>
    val pairedDevices: StateFlow<List<BluetoothDeviceDataClass>>
    val errors: SharedFlow<String>

    fun startDiscovery()
    fun stopDiscovery()

//    fun startBluetoothServer(): Flow<ConnectionResult>
    fun connectToDevice(device: BluetoothDeviceDataClass): Flow<ConnectionResult>

//    suspend fun trySendMessage(message: String): MessageDataClass?

    fun closeConnection()
    fun release()
}