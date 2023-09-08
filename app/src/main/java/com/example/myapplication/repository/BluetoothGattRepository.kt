package com.example.myapplication.repository

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
import com.example.myapplication.domain.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.*
import javax.inject.Inject


@SuppressLint("MissingPermission")
class BluetoothGattRepository @Inject constructor(
    private val context: Context,
    private val application: Application
) : ImplBluetoothRepository {

    private val _isConnected = MutableStateFlow(false)

    private val bluetoothManager by lazy {
        application.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    }
    private val bluetoothAdapter by lazy {
        bluetoothManager.adapter
    }

    private var advertiser: BluetoothLeAdvertiser? = null
    private var advertiseSettings: AdvertiseSettings = buildAdvertiseSettings()
    private var advertiseData: AdvertiseData = buildAdvertiseData()


    val connectMessage = MutableStateFlow(ConnectionState.DISCONNECTED)

    private val scanCallback = object : ScanCallback() {
        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            if (errorCode == 1) {
                _isScanning.value = false
                stopScan()
            }
        }

        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            _isScanning.value = false
            _scannedDevices.update { devices ->
                val newDevice = result.device
                if (newDevice in devices) devices else devices + newDevice
            }
        }

        override fun onBatchScanResults(results: List<ScanResult>) {
            super.onBatchScanResults(results)
            _isScanning.value = false
            results.map { result ->
                _scannedDevices.update { devices ->
                    val newDevice = result.device
                    if (newDevice in devices) devices else devices + newDevice
                }
            }
        }
    }

    private var gattClient: BluetoothGatt? = null
    private var messageCharacteristic: BluetoothGattCharacteristic? = null
    private var bluetoothGattServer: BluetoothGattServer? = null

    private val gattServerCallback = object : BluetoothGattServerCallback() {
        override fun onConnectionStateChange(
            device: BluetoothDevice?,
            status: Int,
            newState: Int
        ) {
            super.onConnectionStateChange(device, status, newState)
            val isSuccess = status == BluetoothGatt.GATT_SUCCESS
            val isConnected = newState == BluetoothProfile.STATE_CONNECTED
            _isConnected.value = isSuccess && isConnected
            if (isSuccess && isConnected) {
                setCurrentChatConnection(device)
            }
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
            super.onCharacteristicWriteRequest(
                device,
                requestId,
                characteristic,
                preparedWrite,
                responseNeeded,
                offset,
                value
            )
            if (characteristic?.uuid == MESSAGE_UUID) {
                bluetoothGattServer?.sendResponse(
                    device,
                    requestId,
                    BluetoothGatt.GATT_SUCCESS,
                    0,
                    null
                )
                val message = value?.toString(Charsets.UTF_8)
                message?.let {
                    Log.d("senoServer", message.toString())
                    _messageData.value = ConnectionResult.TransferSucceeded(
                        MessageDataClass(
                            message = message,
                            senderName = device?.name ?: device?.address ?: "Unknown",
                            isFromLocalUser = true
                        )
                    )
                }

            }
        }
    }

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
            super.onConnectionStateChange(gatt, status, newState)
            val isSuccess = status == BluetoothGatt.GATT_SUCCESS
            val isConnected = newState == BluetoothProfile.STATE_CONNECTED
            if (isSuccess && isConnected) {
                gatt?.discoverServices()
            }
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

        override fun onServicesDiscovered(discoveredGatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(discoveredGatt, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                gattClient = discoveredGatt
                val service = discoveredGatt?.getService(SERVICE_UUID)
                if (service != null)
                    messageCharacteristic = service.getCharacteristic(MESSAGE_UUID)
            }
        }
    }

    private val _scannedDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    override val scannedDevices: StateFlow<List<BluetoothDevice>>
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

    private val _isScanning = MutableStateFlow(false)
    override val isScanning: StateFlow<Boolean>
        get() = _isScanning


    override fun startScan() {
        if (hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            _isScanning.value = true
            startServer()
            val btScanner = bluetoothAdapter?.bluetoothLeScanner
            btScanner?.startScan(buildScanFilters(), buildScanSettings(), scanCallback)
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


    override fun stopScan() {
        if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            return
        }
        if (bluetoothAdapter?.isEnabled == true) {
            val btScanner = bluetoothAdapter?.bluetoothLeScanner
            btScanner?.stopScan(scanCallback)
        }

    }

    override fun startServer() {
        if (hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            bluetoothGattServer =
                bluetoothManager.openGattServer(
                    context, gattServerCallback
                ).apply {
                    addService(setupGattService())
                }
            startAdvertisement()
        }
    }

    override fun setCurrentChatConnection(device: BluetoothDevice?) {
        connectToDevice(device)
    }

    override fun connectToDevice(device: BluetoothDevice?) {
        device?.connectGatt(application, false, bluetoothGattCallback)
    }

    override suspend fun trySendMessage(message: String, deviceAddress: String) {
        if (hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            val bluetoothMessage = MessageDataClass(
                message = message,
                senderName = bluetoothAdapter?.name ?: "Unknown name",
                isFromLocalUser = false
            )
            messageCharacteristic?.let { characteristic ->
                characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT

                val messageBytes = message.toByteArray(Charsets.UTF_8)
                characteristic.value = messageBytes
                this.gattClient?.let {
                    val success = it.writeCharacteristic(messageCharacteristic)
                    if (success) {
                        Log.d("senoClient", message)
                        _messageData.value = ConnectionResult.TransferSucceeded(
                            bluetoothMessage
                        )
                    }
                }
            }
        }
    }

    private fun hasPermission(permission: String): Boolean {
        return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun startAdvertisement() {
        advertiser = bluetoothAdapter?.bluetoothLeAdvertiser
        advertiser?.startAdvertising(advertiseSettings, advertiseData, deviceAdvertiseCallback)
    }

    private fun stopAdvertising() {
        advertiser?.stopAdvertising(deviceAdvertiseCallback)
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