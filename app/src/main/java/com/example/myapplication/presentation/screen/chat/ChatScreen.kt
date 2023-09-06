package com.example.myapplication.presentation.screen.chat

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.domain.MessageDataClass
import com.example.myapplication.presentation.components.ChatBubble
import com.example.myapplication.presentation.screen.devicescan.BluetoothViewModel
import com.example.myapplication.presentation.theme.Blue

@Composable
fun ChatScreen(
    viewModel: BluetoothViewModel
) {
    val message = rememberSaveable {
        mutableStateOf("")
    }
    Box(Modifier.fillMaxSize()) {
        Column {
            ChatBubble(message = MessageDataClass())
        }
        Surface(
            modifier = Modifier.align(
                alignment = Alignment.BottomCenter
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                TextField(
                    value = message.value,
                    onValueChange = { message.value = it },
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(text = "Message")
                    }
                )
                IconButton(onClick = {
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
}