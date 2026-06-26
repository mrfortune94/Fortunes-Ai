package com.fortunesai.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onNavigateSettings: () -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    val messages by viewModel.messages.collectAsState()
    val chatState by viewModel.chatState.collectAsState()
    var input by remember { mutableStateOf("") }
    var selectedModality by remember { mutableStateOf("Chat") }
    
    val modalities = listOf("Chat", "Code", "Image", "Video", "Audio")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FortunesAi") },
                actions = {
                    IconButton(onClick = onNavigateSettings, modifier = Modifier.testTag("settings_button")) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Modality Selector
            ScrollableTabRow(
                selectedTabIndex = modalities.indexOf(selectedModality),
                edgePadding = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                modalities.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedModality == title,
                        onClick = { selectedModality = title },
                        text = { Text(title) }
                    )
                }
            }
            
            if (chatState is ChatState.Error) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = (chatState as ChatState.Error).message,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // Chat Messages
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                reverseLayout = false
            ) {
                items(messages) { msg ->
                    ChatBubble(message = msg)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                if (chatState is ChatState.Loading) {
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }

            // Input Area
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input"),
                    placeholder = { Text("Ask anything (${selectedModality})...") },
                    shape = RoundedCornerShape(24.dp),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                FloatingActionButton(
                    onClick = {
                        viewModel.sendMessage(input, selectedModality)
                        input = ""
                    },
                    modifier = Modifier.testTag("send_button"),
                    containerColor = MaterialTheme.colorScheme.primary,
                    elevation = FloatingActionButtonDefaults.elevation(0.dp)
                ) {
                    Icon(Icons.Filled.Send, contentDescription = "Send")
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: com.fortunesai.api.ChatMessage) {
    val isUser = message.role == "user"
    val background = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    val alignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    val shape = if (isUser) {
        RoundedCornerShape(20.dp, 20.dp, 0.dp, 20.dp)
    } else {
        RoundedCornerShape(20.dp, 20.dp, 20.dp, 0.dp)
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Text(
            text = message.content,
            color = contentColor,
            modifier = Modifier
                .background(background, shape)
                .padding(16.dp)
                .clip(shape)
        )
    }
}
