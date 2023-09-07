package com.example.myapplication.domain

import android.bluetooth.BluetoothDevice
import kotlinx.coroutines.flow.StateFlow

interface BluetoothController {
    val scannedDevices: StateFlow<List<BluetoothDevice>>
    val selectedAddress: StateFlow<String>
    val pairedDevices: StateFlow<List<BluetoothDeviceDataClass>>
    val messageData: StateFlow<ConnectionResult?>

    fun startScan()
    fun startServer()
    fun setCurrentChatConnection(device: BluetoothDevice?)
    fun stopDiscovery()
    fun connectToDevice(device: BluetoothDevice?)
    suspend fun trySendMessage(message: String, deviceAddress: String)
    fun closeConnection()
    fun release()
}