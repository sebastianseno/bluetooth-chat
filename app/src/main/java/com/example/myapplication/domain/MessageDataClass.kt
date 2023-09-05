package com.example.myapplication.domain

data class MessageDataClass(
    val message: String = "",
    val senderName: String = "",
    val isFromLocalUser: Boolean = false
)
