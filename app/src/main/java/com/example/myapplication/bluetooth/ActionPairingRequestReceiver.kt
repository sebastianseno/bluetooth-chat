package com.example.myapplication.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log


@SuppressLint("MissingPermission")
class ActionPairingRequestReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            BluetoothDevice.ACTION_PAIRING_REQUEST -> {
                try {
                    val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(
                            BluetoothDevice.EXTRA_DEVICE,
                            BluetoothDevice::class.java
                        )
                    } else {
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    }
                    val pin = intent.getIntExtra("android.bluetooth.device.extra.PAIRING_KEY", 0)
                    //the pin in case you need to accept for an specific pin

                    Log.d("Bonded", device!!.name)
                    val pinBytes = ("$pin").toByteArray(charset("UTF-8"))
                    device.setPin(pinBytes)
                    //setPairing confirmation if neeeded
                    device.setPairingConfirmation(true)
                    device.createBond()
//                    val type = intent.getIntExtra(
//                        BluetoothDevice.EXTRA_PAIRING_VARIANT,
//                        BluetoothDevice.ERROR
//                    )
//                    Log.d(
//                        "senooPin1",
//                        type.toString()
//                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}