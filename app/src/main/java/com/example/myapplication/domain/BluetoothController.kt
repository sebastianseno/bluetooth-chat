package com.example.myapplication.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface BluetoothController {
    val scannedDevices: StateFlow<List<BluetoothDeviceDataClass>>
    val selectedAddress: StateFlow<String>
    val pairedDevices: StateFlow<List<BluetoothDeviceDataClass>>

    fun startDiscovery()
    fun stopDiscovery()
    fun startBluetoothServer(deviceAddress: String): Flow<ConnectionResult>
    fun listenBluetoothServer(): Flow<ConnectionResult>
    fun connectToDevice(device: BluetoothDeviceDataClass)
    suspend fun trySendMessage(message: String, deviceAddress: String): MessageDataClass?
    fun closeConnection()
    fun release()
}