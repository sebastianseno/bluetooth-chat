package com.example.myapplication.mapper

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import com.example.myapplication.domain.BluetoothDeviceDataClass

@SuppressLint("MissingPermission")
fun BluetoothDevice.toBluetoothDeviceDomain(): BluetoothDeviceDataClass {
    return BluetoothDeviceDataClass(
        name = name,
        address = address,

    )
}