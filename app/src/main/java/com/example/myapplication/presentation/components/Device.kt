package com.example.myapplication.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.presentation.theme.Blue
import com.example.myapplication.presentation.theme.SoftGray

@Composable
fun Device(
    deviceName: String,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Row {
        Surface(
            modifier = Modifier.weight(2f),
            color = SoftGray,
            shape = RoundedCornerShape(30.dp),
            elevation = 0.dp
        ) {
            Text(
                text = deviceName,
                color = Color.Black,
                fontSize = 13.sp,
                modifier = Modifier.padding(vertical = 10.dp, horizontal = 16.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        onClick()
                    },
                color = Blue,
                shape = RoundedCornerShape(10.dp),
                elevation = 0.dp
            ) {
                Text(
                    text = "Connect Now",
                    color = Color.White,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(vertical = 10.dp)
                        .fillMaxWidth()
                )
            }
        }

    }
}