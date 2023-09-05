package com.example.myapplication.presentation.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myapplication.presentation.theme.Blue
import com.example.myapplication.presentation.theme.SoftGray

@Composable
fun Device(deviceName: String) {
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
                modifier = Modifier.padding(vertical = 10.dp, horizontal = 16.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Surface(
            modifier = Modifier.weight(1f),
            color = Blue,
            shape = RoundedCornerShape(10.dp),
            elevation = 0.dp
        ) {
            Text(
                text = "Connect Now",
                color = Color.White,
                modifier = Modifier.padding(vertical = 10.dp, horizontal = 16.dp)
            )
        }
    }
}