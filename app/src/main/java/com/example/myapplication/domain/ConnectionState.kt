package com.example.myapplication.domain;

enum class ConnectionState {
    CONNECTED,
    CONNECTING,
    DISCONNECTING,
    DISCONNECTED,
    CONNECT;

    fun isActive() = (this == CONNECTING || this == CONNECTED)

    fun toTitle() = this.name.lowercase().replaceFirstChar { it.uppercase() }
}