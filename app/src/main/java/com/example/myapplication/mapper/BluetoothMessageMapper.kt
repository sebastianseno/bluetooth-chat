package com.example.myapplication.mapper

import com.example.myapplication.domain.MessageDataClass


fun String.toBluetoothMessage(isFromLocalUser: Boolean): MessageDataClass {
    val name = substringBeforeLast("#")
    val message = substringAfter("#")
    return MessageDataClass(
        message = message,
        senderName = name,
        isFromLocalUser = isFromLocalUser
    )
}

fun MessageDataClass.toByteArray(): ByteArray {
    return "$senderName#$message".encodeToByteArray()
}