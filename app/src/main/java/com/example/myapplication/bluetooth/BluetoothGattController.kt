package com.example.myapplication.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.util.Log
import com.example.myapplication.domain.BluetoothController
import com.example.myapplication.domain.BluetoothDeviceDataClass
import com.example.myapplication.domain.ConnectionResult
import com.example.myapplication.domain.ConnectionState
import com.example.myapplication.domain.MessageDataClass
import com.example.myapplication.mapper.toBluetoothDeviceDomain
import com.example.myapplication.mapper.toByteArray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.update
import java.io.IOException
import java.util.UUID
import javax.inject.Inject


@SuppressLint("MissingPermission")
class BluetoothGattController @Inject constructor(
    private val context: Context,
    private val application: Application,
) : BluetoothController {

    private val _isConnected = MutableStateFlow(false)

    private var dataTransferService: BluetoothDataTransferService? = null
    private var currentServerSocket: BluetoothServerSocket? = null
    private var currentClientSocket: BluetoothSocket? = null
    private val bluetoothManager by lazy {
        context.getSystemService(BluetoothManager::class.java)
    }
    private val bluetoothAdapter by lazy {
        bluetoothManager?.adapter
    }

    val connectMessage = MutableStateFlow(ConnectionState.DISCONNECTED)

    private val bluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
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
    private val onActionPairingReceiver = ActionPairingRequestReceiver()

    private val _scannedDevices = MutableStateFlow<List<BluetoothDeviceDataClass>>(emptyList())
    override val scannedDevices: StateFlow<List<BluetoothDeviceDataClass>>
        get() = _scannedDevices.asStateFlow()


    private val _selectedAddress = MutableStateFlow("")
    override val selectedAddress: StateFlow<String>
        get() = _selectedAddress.asStateFlow()

    private val _pairedDevices = MutableStateFlow<List<BluetoothDeviceDataClass>>(emptyList())
    override val pairedDevices: StateFlow<List<BluetoothDeviceDataClass>>
        get() = _pairedDevices.asStateFlow()

    init {
        updatePairedDevices()
    }
    override fun startDiscovery() {
        if (hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            context.registerReceiver(
                foundDeviceReceiver,
                IntentFilter(BluetoothDevice.ACTION_FOUND)
            )
            updatePairedDevices()
            bluetoothAdapter?.startDiscovery()
        }
    }

    override fun stopDiscovery() {
        if(!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            return
        }
        bluetoothAdapter?.cancelDiscovery()
    }

    override fun startBluetoothServer(deviceAddress: String): Flow<ConnectionResult> {
        return flow {
            Log.d("seno", "here")
            if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                throw SecurityException("No BLUETOOTH_CONNECT permission")
            }
            currentClientSocket = bluetoothAdapter
                ?.getRemoteDevice(deviceAddress)
                ?.createRfcommSocketToServiceRecord(
                    UUID.fromString(SERVICE_UUID)
                )

            currentServerSocket = bluetoothAdapter?.listenUsingRfcommWithServiceRecord(
                "chat_service",
                UUID.fromString(SERVICE_UUID)
            )
            stopDiscovery()
            currentClientSocket?.let { socket ->
                try {
                    socket.connect()
                    Log.d("seenxx", "heree")
                    emit(ConnectionResult.ConnectionEstablished)
                    BluetoothDataTransferService(socket).also { data ->
                        dataTransferService = data
                        emitAll(
                            data.listenForIncomingMessages()
                                .map { ConnectionResult.TransferSucceeded(it) }
                        )
                    }
                } catch (e: IOException) {
                    socket.close()
                    currentClientSocket = null
                    emit(ConnectionResult.Error("Connection was interrupted"))
                }
            }
        }.onCompletion {
            closeConnection()
        }.flowOn(Dispatchers.IO)
    }

    override fun listenBluetoothServer(): Flow<ConnectionResult> {
        return flow {
            if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                throw SecurityException("No BLUETOOTH_CONNECT permission")
            }

            currentServerSocket = bluetoothAdapter?.listenUsingRfcommWithServiceRecord(
                "chat_service",
                UUID.fromString(SERVICE_UUID)
            )

            var shouldLoop = true
            while (shouldLoop) {
                currentClientSocket = try {
                    currentServerSocket?.accept()
                } catch (e: IOException) {
                    shouldLoop = false
                    null
                }
                emit(ConnectionResult.ConnectionEstablished)
                currentClientSocket?.let {
                    currentServerSocket?.close()
                    val service = BluetoothDataTransferService(it)
                    dataTransferService = service

                    emitAll(
                        service
                            .listenForIncomingMessages()
                            .map { data ->
                                ConnectionResult.TransferSucceeded(data)
                            }
                    )
                }
            }
        }.onCompletion {
            closeConnection()
        }.flowOn(Dispatchers.IO)
    }

    @SuppressLint("MissingPermission")
    override fun connectToDevice(device: BluetoothDeviceDataClass) {
        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            return
        }

        if (bluetoothAdapter?.isEnabled == true) {
            bluetoothAdapter?.let { adapter ->
                try {
                    val gattDevice = adapter.getRemoteDevice(device.address)
                    context.registerReceiver(
                        onActionPairingReceiver,
                        IntentFilter(BluetoothDevice.ACTION_PAIRING_REQUEST)
                    )
                    gattDevice.connectGatt(application, false, bluetoothGattCallback)
                } catch (e: Exception) {
                    _isConnected.value = false
                }
            }
        }
    }

    override suspend fun trySendMessage(message: String, deviceAddress: String): MessageDataClass? {
        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            return null
        }
        if (dataTransferService == null) {
            return null
        }
        val bluetoothMessage = MessageDataClass(
            message = message,
            senderName = bluetoothAdapter?.name ?: "Unknown name",
            isFromLocalUser = true
        )
        dataTransferService?.sendMessage(bluetoothMessage.toByteArray())
        return bluetoothMessage
    }

    override fun closeConnection() {
        currentClientSocket?.close()
        currentServerSocket?.close()
        currentClientSocket = null
        currentServerSocket = null
    }

    override fun release() {
    }

    private fun hasPermission(permission: String): Boolean {
        return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        const val SERVICE_UUID = "27b7d1da-08c7-4505-a6d1-2459987e5e2d"
    }
    private fun updatePairedDevices() {
        if(!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            return
        }
        bluetoothAdapter
            ?.bondedDevices
            ?.map { it.toBluetoothDeviceDomain() }
            ?.also { devices ->
                _pairedDevices.update { devices }
            }
    }
}