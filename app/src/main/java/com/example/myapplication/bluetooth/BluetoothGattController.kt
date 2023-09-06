package com.example.myapplication.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.util.Log
import com.example.myapplication.domain.BluetoothDeviceDataClass
import com.example.myapplication.domain.ConnectionState
import com.example.myapplication.mapper.toBluetoothDeviceDomain
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@SuppressLint("MissingPermission")
class BluetoothGattController @Inject constructor(
    private val context: Context,
    private val application: Application
) : BluetoothController {

    private val _isConnected = MutableStateFlow(false)

    private val bluetoothManager by lazy {
        context.getSystemService(BluetoothManager::class.java)
    }
    private val bluetoothAdapter by lazy {
        bluetoothManager?.adapter
    }

    val connectMessage = MutableStateFlow(ConnectionState.DISCONNECTED)

    private val bluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            Log.d("senoo", newState.toString())
            when (newState) {
                BluetoothProfile.STATE_CONNECTING -> connectMessage.value =
                    ConnectionState.CONNECTING

                BluetoothProfile.STATE_CONNECTED -> {
                    connectMessage.value = ConnectionState.CONNECTED
                }
                BluetoothProfile.STATE_DISCONNECTING -> connectMessage.value =
                    ConnectionState.DISCONNECTING

                BluetoothProfile.STATE_DISCONNECTED -> connectMessage.value =
                    ConnectionState.DISCONNECTED

                else -> connectMessage.value = ConnectionState.CONNECT
            }
        }
    }
    private val foundDeviceReceiver = FoundDeviceReceiver { device ->
        _scannedDevices.update { devices ->
            val newDevice = device.toBluetoothDeviceDomain()
            if (newDevice in devices) devices else devices + newDevice
        }
    }

//    private val scanCallbacks = object : ScanCallback() {
//        override fun onScanResult(callbackType: Int, result: ScanResult?) {
//            if (result != null) {
//                _scannedDevices.update { devices ->
//                    val newDevice = result.device!!.toBluetoothDeviceDomain()
//                    if (newDevice in devices) devices else devices + newDevice
//                }
//            }
//        }
//    }
    override val isConnected: StateFlow<Boolean>
        get() = TODO("Not yet implemented")

    private val _scannedDevices = MutableStateFlow<List<BluetoothDeviceDataClass>>(emptyList())
    override val scannedDevices: StateFlow<List<BluetoothDeviceDataClass>>
        get() = _scannedDevices.asStateFlow()

    override val pairedDevices: StateFlow<List<BluetoothDeviceDataClass>>
        get() = TODO("Not yet implemented")

    private val _selectedAddress = MutableStateFlow("")
    override val selectedAddress: StateFlow<String>
        get() = _selectedAddress.asStateFlow()
    override val errors: SharedFlow<String>
        get() = TODO("Not yet implemented")

    //    override fun startDiscovery() {
//        if (hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
//
//            context.registerReceiver(
//                foundDeviceReceiver,
//                IntentFilter(BluetoothDevice.ACTION_FOUND)
//            )
//
////            updatePairedDevices()
//
//            bluetoothAdapter?.startDiscovery()
//        }
//    }
    override fun startDiscovery() {
        if (hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
//            bluetoothAdapter?.bluetoothLeScanner?.startScan(scanCallbacks)
            context.registerReceiver(
                foundDeviceReceiver,
                IntentFilter(BluetoothDevice.ACTION_FOUND)
            )
            bluetoothAdapter?.startDiscovery()
        }
    }

    override fun stopDiscovery() {
        try {
//            if (bluetoothAdapter?.isEnabled == true)
//                bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallbacks)
        } catch (e: Exception) {

        } finally {

        }
    }

//    override fun startBluetoothServer(): Flow<ConnectionResult> {
//        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
//            throw SecurityException("No BLUETOOTH_CONNECT permission")
//        }
//
//    }

    @SuppressLint("MissingPermission")
    override fun connectToDevice(device: BluetoothDeviceDataClass) {
        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            return
        }
        if (bluetoothAdapter?.isEnabled == true) {
            bluetoothAdapter?.let { adapter ->
                try {
                    val gattDevice = adapter.getRemoteDevice(device.address)
                    gattDevice.connectGatt(application, false, bluetoothGattCallback)
                } catch (e: Exception) {
                    _isConnected.value = false
                }
            }
        }
    }

//    override suspend fun trySendMessage(message: String): MessageDataClass? {
//    }

    override fun closeConnection() {
    }

    override fun release() {
    }

    private fun hasPermission(permission: String): Boolean {
        return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }

}