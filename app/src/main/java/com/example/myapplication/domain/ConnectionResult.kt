package com.example.myapplication.domain

sealed interface ConnectionResult {
    data object ConnectionEstablished : ConnectionResult
    data class TransferSucceeded(val message: MessageDataClass) : ConnectionResult
    data class Error(val message: String) : ConnectionResult
}