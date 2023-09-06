package com.example.myapplication.presentation.screen.devicescan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.bluetooth.BluetoothGattController
import com.example.myapplication.domain.BluetoothDeviceDataClass
import com.example.myapplication.presentation.screen.BluetoothUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class BluetoothViewModel @Inject constructor(
    private val bluetoothController: BluetoothGattController
) : ViewModel() {

    private val _state = MutableStateFlow(BluetoothUiState())
    private val _bleMessage = bluetoothController.connectMessage

    val state = combine(
        bluetoothController.scannedDevices,
        _state,
        _bleMessage,
    ) { scannedDevices, state, message ->
        state.copy(
            scannedDevices = scannedDevices,
            connectionStatus = message,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _state.value)

    fun startScan() {
        bluetoothController.startDiscovery()
    }

    fun connectToDevice(device: BluetoothDeviceDataClass) {
        bluetoothController.connectToDevice(device)
    }

}