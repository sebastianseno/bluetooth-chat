package com.example.myapplication.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface BluetoothController {
    val scannedDevices: StateFlow<List<BluetoothDeviceDataClass>>
    val selectedAddress: StateFlow<String>
    val pairedDevices: StateFlow<List<BluetoothDeviceDataClass>>
    val messageData: StateFlow<ConnectionResult?>

    fun startScan()
    fun stopDiscovery()
    fun startGattServer(): Flow<ConnectionResult>
    fun connectToDevice(device: BluetoothDeviceDataClass)
    suspend fun trySendMessage(message: String, deviceAddress: String)
    fun closeConnection()
    fun release()
}