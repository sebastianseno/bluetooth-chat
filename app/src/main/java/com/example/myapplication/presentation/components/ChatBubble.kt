package com.example.myapplication.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.domain.MessageDataClass
import com.example.myapplication.presentation.theme.Blue
import com.example.myapplication.presentation.theme.Gray

@Composable
fun ChatBubble(
    message: MessageDataClass,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(
                RoundedCornerShape(
                    topStart = if (message.isFromLocalUser)  0.dp  else 15.dp,
                    topEnd = 15.dp,
                    bottomStart = 15.dp,
                    bottomEnd = if (message.isFromLocalUser) 15.dp else 0.dp
                )
            )
            .background(
                if (message.isFromLocalUser) Gray else Blue
            )
            .padding(16.dp)
    ) {
        Text(
            text = message.senderName,
            fontSize = 10.sp,
            color = if (message.isFromLocalUser) Color.Black else Color.White
        )
        Text(
            text = message.message,
            color = if (message.isFromLocalUser) Color.Black else Color.White,
            fontSize = 13.sp,
            modifier = Modifier.widthIn(max = 250.dp)
        )
    }
}