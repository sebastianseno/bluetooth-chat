package com.example.myapplication.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import com.example.myapplication.domain.BluetoothDeviceDataClass
import com.example.myapplication.domain.ConnectionResult
import com.example.myapplication.domain.MessageDataClass
import com.example.myapplication.mapper.toBluetoothDeviceDomain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@SuppressLint("MissingPermission")
class BluetoothGattController @Inject constructor(
    private val context: Context
) : BluetoothController {

    private val _isConnected = MutableStateFlow(false)

    private val bluetoothManager by lazy {
        context.getSystemService(BluetoothManager::class.java)
    }
    private val bluetoothAdapter by lazy {
        bluetoothManager?.adapter
    }
    private val bluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                _isConnected.value = true
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                _isConnected.value = false
            }
        }
    }


    private val scanCallbacks = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            if (result != null) {
                _scannedDevices.update { devices ->
                    val newDevice = result.device!!.toBluetoothDeviceDomain()
                    if (newDevice in devices) devices else devices + newDevice
                }
            }
        }
    }
    override val isConnected: StateFlow<Boolean>
        get() = TODO("Not yet implemented")

    private val _scannedDevices = MutableStateFlow<List<BluetoothDeviceDataClass>>(emptyList())
    override val scannedDevices: StateFlow<List<BluetoothDeviceDataClass>>
        get() = _scannedDevices.asStateFlow()

    override val pairedDevices: StateFlow<List<BluetoothDeviceDataClass>>
        get() = TODO("Not yet implemented")
    override val errors: SharedFlow<String>
        get() = TODO("Not yet implemented")

    override fun startDiscovery() {
        if (hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            bluetoothAdapter?.bluetoothLeScanner?.startScan(scanCallbacks)
        }
    }

    override fun stopDiscovery() {
        try {
            if ( bluetoothAdapter?.isEnabled == true)
                bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallbacks)
        } catch (e: Exception) {
        }
        finally {

        }
    }

//    override fun startBluetoothServer(): Flow<ConnectionResult> {
//        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
//            throw SecurityException("No BLUETOOTH_CONNECT permission")
//        }
//
//    }

    @SuppressLint("MissingPermission")
    override fun connectToDevice(device: BluetoothDeviceDataClass): Flow<ConnectionResult> {
        return flow {
            if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                return@flow
            }
            if (bluetoothAdapter?.isEnabled == true) {
                bluetoothAdapter?.let { adapter ->
                    try {
                        if (_isConnected.value) {
                            val gattDevice = adapter.getRemoteDevice(device.address)
                            gattDevice.connectGatt(context, false, bluetoothGattCallback)
                            emit(ConnectionResult.ConnectionEstablished)
                        } else {
                            emit(ConnectionResult.Error("gagal"))
                        }
                    } catch (e: Exception) {
                        _isConnected.value = false

                    }
                }
            }
        }.flowOn(Dispatchers.IO)
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