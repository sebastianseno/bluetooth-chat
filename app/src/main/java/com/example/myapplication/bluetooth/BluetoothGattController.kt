package com.example.myapplication.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.ContentValues.TAG
import android.content.Context
import android.content.pm.PackageManager
import android.os.ParcelUuid
import android.util.Log
import com.example.myapplication.domain.BluetoothController
import com.example.myapplication.domain.BluetoothDeviceDataClass
import com.example.myapplication.domain.ConnectionResult
import com.example.myapplication.domain.ConnectionState
import com.example.myapplication.domain.MessageDataClass
import com.example.myapplication.mapper.toBluetoothDeviceDomain
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.UUID
import javax.inject.Inject


@SuppressLint("MissingPermission")
class BluetoothGattController @Inject constructor(
    private val context: Context,
    private val application: Application,
    private val scope: CoroutineScope
) : BluetoothController {

    private val _isConnected = MutableStateFlow(false)

    private var currentServerSocket: BluetoothServerSocket? = null
    private var currentClientSocket: BluetoothSocket? = null
    private val bluetoothManager by lazy {
        context.getSystemService(BluetoothManager::class.java)
    }
    private val bluetoothAdapter by lazy {
        bluetoothManager?.adapter
    }

    private var advertiser: BluetoothLeAdvertiser? = null
    private var advertiseSettings: AdvertiseSettings = buildAdvertiseSettings()
    private var advertiseData: AdvertiseData = buildAdvertiseData()


    val connectMessage = MutableStateFlow(ConnectionState.DISCONNECTED)

    private val scanCallback = object : ScanCallback() {
        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
//            if (errorCode == 1) {
//                stopDiscovery()
//            }
        }

        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            _scannedDevices.update { devices ->
                Log.i("senox", result.device.toString())
                val newDevice = result.device.toBluetoothDeviceDomain()
                if (newDevice in devices) devices else devices + newDevice
            }
        }

        override fun onBatchScanResults(results: List<ScanResult>) {
            super.onBatchScanResults(results)
            results.map { result ->
                _scannedDevices.update { devices ->
                    val newDevice = result.device.toBluetoothDeviceDomain()
                    if (newDevice in devices) devices else devices + newDevice
                }
            }
            Log.i("seno", results.toString())
        }
    }

    private var gattClient: BluetoothGatt? = null
    private var messageCharacteristic: BluetoothGattCharacteristic? = null
    private var bluetoothGattServer: BluetoothGattServer? = null
    private val deviceAdvertiseCallback = object : AdvertiseCallback() {
        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)
            val errorMessage = "Advertise failed with error: $errorCode"
            Log.d(TAG, "failed $errorMessage")
        }

        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            super.onStartSuccess(settingsInEffect)
            Log.d(TAG, "successfully started")
        }
    }
    private val bluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            val isSuccess = status == BluetoothGatt.GATT_SUCCESS
            when (newState) {
                BluetoothProfile.STATE_CONNECTING -> connectMessage.value =
                    ConnectionState.CONNECTING

                BluetoothProfile.STATE_CONNECTED -> {
                    connectMessage.value = ConnectionState.CONNECTED
                    if (isSuccess) gatt?.discoverServices()
                }
                BluetoothProfile.STATE_DISCONNECTING -> connectMessage.value =
                    ConnectionState.DISCONNECTING

                BluetoothProfile.STATE_DISCONNECTED -> connectMessage.value =
                    ConnectionState.DISCONNECTED

                else -> connectMessage.value = ConnectionState.CONNECT
            }
        }

        override fun onServicesDiscovered(discoveredGatt: BluetoothGatt?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                this@BluetoothGattController.gattClient = discoveredGatt
                val service = discoveredGatt?.getService(SERVICE_UUID)
                if (service != null)
                    messageCharacteristic = service.getCharacteristic(MESSAGE_UUID)
            }
        }
    }

    private val _scannedDevices = MutableStateFlow<List<BluetoothDeviceDataClass>>(emptyList())
    override val scannedDevices: StateFlow<List<BluetoothDeviceDataClass>>
        get() = _scannedDevices.asStateFlow()


    private val _selectedAddress = MutableStateFlow("")
    override val selectedAddress: StateFlow<String>
        get() = _selectedAddress.asStateFlow()

    private val _pairedDevices = MutableStateFlow<List<BluetoothDeviceDataClass>>(emptyList())
    override val pairedDevices: StateFlow<List<BluetoothDeviceDataClass>>
        get() = _pairedDevices.asStateFlow()

    private val _messageData = MutableStateFlow<ConnectionResult?>(null)
    override val messageData: StateFlow<ConnectionResult?>
        get() = _messageData


    override fun startScan() {
        if (hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            val btScanner = bluetoothAdapter?.bluetoothLeScanner
            btScanner?.startScan(null, buildScanSettings(), scanCallback)
        }
    }

    private fun buildScanFilters(): List<ScanFilter> {
        val builder = ScanFilter.Builder()
        builder.setServiceUuid(ParcelUuid(SERVICE_UUID))
        val filter = builder.build()
        return listOf(filter)
    }

    private fun buildScanSettings(): ScanSettings {
        return ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
            .build()
    }

    private fun setupGattService(): BluetoothGattService {
        val service = BluetoothGattService(SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)
        val messageCharacteristic = BluetoothGattCharacteristic(
            MESSAGE_UUID,
            BluetoothGattCharacteristic.PROPERTY_WRITE,
            BluetoothGattCharacteristic.PERMISSION_WRITE
        )
        service.addCharacteristic(messageCharacteristic)

        return service
    }


    override fun stopDiscovery() {
        if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            return
        }
//        if (bluetoothAdapter?.isEnabled == true) {
//            val btScanner = bluetoothAdapter?.bluetoothLeScanner
//            btScanner?.stopScan(scanCallback)
//        }

    }

    override fun startGattServer(): Flow<ConnectionResult> {
        return flow {
            if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                throw SecurityException("No BLUETOOTH_CONNECT permission")
            }
            val gattServerCallback = object : BluetoothGattServerCallback() {
                override fun onConnectionStateChange(
                    device: BluetoothDevice?,
                    status: Int,
                    newState: Int
                ) {
                    val isSuccess = status == BluetoothGatt.GATT_SUCCESS
                    val isConnected = newState == BluetoothProfile.STATE_CONNECTED
                    _isConnected.value = isSuccess && isConnected
                }

                override fun onCharacteristicWriteRequest(
                    device: BluetoothDevice?,
                    requestId: Int,
                    characteristic: BluetoothGattCharacteristic?,
                    preparedWrite: Boolean,
                    responseNeeded: Boolean,
                    offset: Int,
                    value: ByteArray?
                ) {
                    try {
                        bluetoothGattServer?.sendResponse(
                            device,
                            requestId,
                            BluetoothGatt.GATT_SUCCESS,
                            0,
                            null
                        )
                        val message = value?.toString(Charsets.UTF_8)
                        scope.launch {
                            emit(
                                ConnectionResult.TransferSucceeded(
                                    MessageDataClass(
                                        message = message.orEmpty(),
                                        senderName = device?.name ?: device?.address ?: "Unknown",
                                        isFromLocalUser = true
                                    )
                                )
                            )
                        }
                    } catch (e: IOException) {
                        scope.launch {
                            emit(ConnectionResult.Error("Connection was interrupted"))
                        }
                    }
                }
            }
            bluetoothGattServer =
                bluetoothManager.openGattServer(
                    context, gattServerCallback
                ).apply {
                    addService(setupGattService())
                }
            startAdvertisement()
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
                    gattDevice.connectGatt(application, false, bluetoothGattCallback)
                } catch (e: Exception) {
                    _isConnected.value = false
                }
            }
        }
    }

    override suspend fun trySendMessage(message: String, deviceAddress: String) {
        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            val bluetoothMessage = MessageDataClass(
                message = message,
                senderName = bluetoothAdapter?.name ?: "Unknown name",
                isFromLocalUser = true
            )
            messageCharacteristic?.let { characteristic ->
                characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT

                val messageBytes = message.toByteArray(Charsets.UTF_8)
                characteristic.value = messageBytes
                this.gattClient?.let {
                    val success = it.writeCharacteristic(messageCharacteristic)
                    if (!success) {
                        _messageData.value = ConnectionResult.TransferSucceeded(
                            bluetoothMessage
                        )
                    }
                }
            }
        }
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

    private fun startAdvertisement() {
        advertiser = bluetoothAdapter?.bluetoothLeAdvertiser
        advertiser?.startAdvertising(advertiseSettings, advertiseData, deviceAdvertiseCallback)
    }

    private fun buildAdvertiseData(): AdvertiseData {
        val dataBuilder = AdvertiseData.Builder()
            .addServiceUuid(ParcelUuid(SERVICE_UUID))
            .setIncludeDeviceName(true)

        return dataBuilder.build()
    }

    private fun buildAdvertiseSettings(): AdvertiseSettings {
        return AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER)
            .setTimeout(0)
            .build()
    }

    companion object {
        val SERVICE_UUID: UUID = UUID.fromString("0000b81d-0000-1000-8000-00805f9b34fb")
        val MESSAGE_UUID: UUID = UUID.fromString("7db3e235-3608-41f3-a03c-955fcbd2ea4b")
    }

}