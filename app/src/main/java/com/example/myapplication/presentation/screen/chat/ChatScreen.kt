package com.example.myapplication.presentation.screen.chat

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.navigation.NavController
import com.example.myapplication.R
import com.example.myapplication.domain.MessageDataClass
import com.example.myapplication.presentation.components.ChatBubble
import com.example.myapplication.presentation.screen.viewmodel.BluetoothViewModel
import com.example.myapplication.presentation.theme.Blue

@Composable
fun ChatScreen(
    viewModel: BluetoothViewModel = hiltViewModel(),
    deviceAddress: String,
    navController: NavController
) {
    val message = rememberSaveable {
        mutableStateOf("")
    }
    val state = viewModel.state.collectAsState()
    LaunchedEffect(key1 = Unit, block = {
        viewModel.startServer()
        viewModel.listenBluetoothServer()
    })

    Box(
        Modifier
            .fillMaxSize()
    ) {
        BackHandler(true) {
            navController.popBackStack()
            viewModel.clearChat()
        }
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                modifier = Modifier
                    .fillMaxWidth(),
                backgroundColor = Blue,
                navigationIcon = {
                    Icon(
                        modifier = Modifier
                            .clickable {
                                navController.popBackStack()
                                viewModel.clearChat()
                            }
                            .fillMaxHeight()
                            .padding(start = 8.dp),
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = null,
                        tint = Color.White
                    )
                },
                title = {
                    Text(
                        text = deviceAddress,
                        color = Color.White
                    )
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                state.value.chatMessages.forEach {
                    ChatBubble(
                        message = it ?: MessageDataClass(),
                        modifier = Modifier.align(if (it?.isFromLocalUser == true) Alignment.Start else Alignment.End)
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
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
                    viewModel.sendMessage(
                        message = message.value,
                        deviceAddress = deviceAddress
                    )
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