package com.example.myapplication.presentation.screen.chat

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.R
import com.example.myapplication.domain.MessageDataClass
import com.example.myapplication.presentation.components.ChatBubble
import com.example.myapplication.presentation.screen.viewmodel.BluetoothViewModel
import com.example.myapplication.presentation.theme.Blue
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
fun ChatScreen(
    viewModel: BluetoothViewModel = hiltViewModel(),
    deviceAddress: String
) {
    val message = rememberSaveable {
        mutableStateOf("")
    }
    val systemUiController = rememberSystemUiController()
    val state = viewModel.state.collectAsState()
    LaunchedEffect(key1 = Unit, block = {
        if (deviceAddress != "null") {
            viewModel.waitForIncomingConnections(deviceAddress)
        } else {
            viewModel.listenBluetoothServer()
        }
    })
    Box(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        systemUiController.setStatusBarColor(
            color = if (state.value.isConnected) {
                Color.Green
            } else {
                Color.Red
            }
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            state.value.chatMessages.forEach {
                ChatBubble(
                    message = it ?: MessageDataClass(),
                    modifier = Modifier.align(if (it?.isFromLocalUser == true) Alignment.End else Alignment.Start)
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
        ) {
            TextField(
                value = message.value,
                onValueChange = { message.value = it },
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.textFieldColors(textColor = Color.Black),
                placeholder = {
                    Text(text = "Message")
                }
            )
            IconButton(
                onClick = {
                    viewModel.sendMessage(message = message.value, deviceAddress = deviceAddress)
                    message.value = ""
                }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_send),
                    tint = Blue,
                    contentDescription = "Send message"
                )
            }
        }
    }
}