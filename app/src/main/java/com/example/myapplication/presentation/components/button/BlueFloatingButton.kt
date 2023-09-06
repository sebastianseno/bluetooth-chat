package com.example.myapplication.presentation.components.button

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myapplication.presentation.theme.Blue
import com.example.myapplication.presentation.theme.SoftGray

@Composable
fun BlueFloatingButton(
    modifier: Modifier = Modifier,
    label: String = "Scan Now",
    onClick: () -> Unit
) {
    Button(
        shape = RoundedCornerShape(30.dp),
        colors = ButtonDefaults.buttonColors(Blue),
        onClick = { onClick() },
        border = BorderStroke(1.dp, SoftGray),
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            color = Color.White,
            modifier = Modifier.padding(vertical = 10.dp)
        )
    }
}