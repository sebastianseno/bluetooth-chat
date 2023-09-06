package com.example.myapplication.presentation.screen.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.bluetooth.BluetoothGattController
import com.example.myapplication.domain.BluetoothDeviceDataClass
import com.example.myapplication.domain.ConnectionResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BluetoothViewModel @Inject constructor(
    private val bluetoothController: BluetoothGattController,
) : ViewModel() {

    private val _state = MutableStateFlow(BluetoothUiState())
    private val _bleMessage = bluetoothController.connectMessage
    private var deviceConnectionJob: Job? = null

    val state = combine(
        bluetoothController.scannedDevices,
        bluetoothController.pairedDevices,
        _state,
        _bleMessage,
    ) { scannedDevices, pairedDevice, state, message ->
        state.copy(
            scannedDevices = scannedDevices,
            connectionStatus = message,
            chatMessages = state.chatMessages,
            pairedDevices = pairedDevice
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _state.value)

    fun startScan() {
        bluetoothController.startDiscovery()
    }

    fun connectToDevice(device: BluetoothDeviceDataClass) {
        bluetoothController.connectToDevice(device)
    }

    fun waitForIncomingConnections(deviceAddress: String?) {
        viewModelScope.launch {
            if (deviceAddress != null) {
                deviceConnectionJob = bluetoothController
                    .startBluetoothServer(deviceAddress)
                    .listen()
            }
        }
    }

    fun listenBluetoothServer() {
        deviceConnectionJob = bluetoothController
            .listenBluetoothServer()
            .listen()
    }

    fun sendMessage(message: String, deviceAddress: String) {
        viewModelScope.launch {
            val bluetoothMessage = bluetoothController.trySendMessage(message, deviceAddress)
            if (bluetoothMessage != null) {
                _state.update {
                    it.copy(
                        chatMessages = (it.chatMessages + bluetoothMessage)
                    )
                }
            }
        }
    }

    private fun Flow<ConnectionResult>.listen(): Job {
        return onEach { result ->
            when (result) {
                ConnectionResult.ConnectionEstablished -> {
                    _state.update {
                        it.copy(
                            isConnected = true,
                            isConnecting = false,
                        )
                    }
                }

                is ConnectionResult.TransferSucceeded -> {
                    _state.update {
                        it.copy(
                            chatMessages = it.chatMessages + result.message
                        )
                    }
                }

                is ConnectionResult.Error -> {
                    _state.update {
                        it.copy(
                            isConnected = false,
                            isConnecting = false,
                        )
                    }
                }
            }
        }.catch { throwable ->
            bluetoothController.closeConnection()
            _state.update {
                it.copy(
                    isConnected = false,
                    isConnecting = false,
                )
            }
        }.launchIn(viewModelScope)
    }

}