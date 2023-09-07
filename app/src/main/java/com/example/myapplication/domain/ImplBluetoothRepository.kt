package com.example.myapplication.domain

import android.bluetooth.BluetoothDevice
import kotlinx.coroutines.flow.StateFlow

interface ImplBluetoothRepository {
    val scannedDevices: StateFlow<List<BluetoothDevice>>
    val selectedAddress: StateFlow<String>
    val pairedDevices: StateFlow<List<BluetoothDeviceDataClass>>
    val messageData: StateFlow<ConnectionResult?>
    val isScanning: StateFlow<Boolean>
    fun startScan()
    fun startServer()
    fun setCurrentChatConnection(device: BluetoothDevice?)
    fun stopScan()
    fun connectToDevice(device: BluetoothDevice?)
    suspend fun trySendMessage(message: String, deviceAddress: String)

}