package com.example.myapplication.presentation.screen.chat

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.myapplication.domain.MessageDataClass
import com.example.myapplication.presentation.components.ChatBubble

@Composable
fun ChatScreen() {
    Scaffold {
        ChatBubble(modifier = Modifier.padding(it), message = MessageDataClass())
    }
}