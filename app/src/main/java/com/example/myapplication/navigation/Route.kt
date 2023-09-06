package com.example.myapplication.navigation

sealed class Route(val route: String) {
    data object DeviceScanScreen : Route("device_scan_screen")
    data object ChatScreen : Route("chat_screen")
}