package com.example.myapplication.presentation.components

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonColors
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myapplication.presentation.theme.Blue

@Composable
fun FloatingButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        shape = RoundedCornerShape(30.dp),
        colors = ButtonDefaults.buttonColors(Color.White),
        onClick = { onClick() },
        modifier = modifier
            .fillMaxWidth()
    ) {
        Text(
            text = "Scan Now",
            color = Blue
        )
    }
}