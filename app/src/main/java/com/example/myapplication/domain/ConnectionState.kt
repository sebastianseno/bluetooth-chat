package com.example.myapplication.domain;

enum class ConnectionState {
    CONNECTED,
    CONNECTING,
    DISCONNECTING,
    DISCONNECTED,
    CONNECT;

    fun toTitle() = this.name.lowercase().replaceFirstChar { it.uppercase() }
}