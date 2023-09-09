package com.example.myapplication.presentation.screen.viewmodel

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.ConnectionResult
import com.example.myapplication.repository.BluetoothGattRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BluetoothViewModel @Inject constructor(
    private val bluetoothController: BluetoothGattRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(BluetoothUiState())
    private val _bleMessage = bluetoothController.connectMessage
    private var deviceConnectionJob: Job? = null

    val state = combine(
        bluetoothController.scannedDevices,
        _state,
        _bleMessage,
        bluetoothController.isScanning
    ) { scannedDevices, state, connectMessage, isScanning ->
        state.copy(
            scannedDevices = scannedDevices,
            connectionStatus = connectMessage,
            isScanning = isScanning
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _state.value)

    fun startScan() {
        bluetoothController.startScan()
    }

    fun startServer() {
        bluetoothController.startServer()
    }

    fun setCurrentUser(device: BluetoothDevice)  {
        bluetoothController.setCurrentChatConnection(device = device)
    }

    fun sendMessage(message: String, deviceAddress: String) {
        viewModelScope.launch {
            bluetoothController.trySendMessage(message, deviceAddress)
        }
    }

    fun listenBluetoothServer() {
        viewModelScope.launch {
        deviceConnectionJob = bluetoothController
            .messageData.listen()
        }
    }

    fun clearChat() {
//        bluetoothController.close()
    }

    private fun Flow<ConnectionResult?>.listen(): Job {
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
                else -> {
                    _state.update {
                        it.copy(
                            isConnected = false,
                            isConnecting = false,
                        )
                    }
                }
            }
        }.catch { throwable ->
            bluetoothController.startServer()
            _state.update {
                it.copy(
                    isConnected = false,
                    isConnecting = false,
                )
            }
        }.launchIn(viewModelScope)
    }

    override fun onCleared() {
        super.onCleared()
        bluetoothController.stopScan()
    }
}