package com.fortunesai.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    val xaiKey by viewModel.xaiApiKey.collectAsState()
    val moderationLevel by viewModel.moderationLevel.collectAsState()
    val elevenLabsKey by viewModel.elevenLabsApiKey.collectAsState()
    val voiceId by viewModel.voiceId.collectAsState()
    val receptionistActive by viewModel.receptionistActive.collectAsState()
    val autoAnswerCalls by viewModel.autoAnswerCalls.collectAsState()
    val receptionistGreeting by viewModel.receptionistGreeting.collectAsState()
    
    var currentKey by remember(xaiKey) { mutableStateOf(xaiKey) }
    var currentModLevel by remember(moderationLevel) { mutableStateOf(moderationLevel) }
    var currentElevenLabsKey by remember(elevenLabsKey) { mutableStateOf(elevenLabsKey) }
    var currentVoiceId by remember(voiceId) { mutableStateOf(voiceId) }
    var currentReceptionistActive by remember(receptionistActive) { mutableStateOf(receptionistActive) }
    var currentAutoAnswer by remember(autoAnswerCalls) { mutableStateOf(autoAnswerCalls) }
    var currentGreeting by remember(receptionistGreeting) { mutableStateOf(receptionistGreeting) }

    var newProfileName by remember { mutableStateOf("") }
    var newProfileVoiceId by remember { mutableStateOf("") }
    val voiceProfiles by viewModel.voiceProfiles.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Text(
                text = "xAI Configuration",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = currentKey,
                onValueChange = { currentKey = it },
                label = { Text("xAI API Key") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("api_key_input"),
                singleLine = true
            )
            Text(
                text = "Your key is stored securely and locally.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Content Moderation Level",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Adjust the strictness of the AI responses.",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Slider(
                value = currentModLevel,
                onValueChange = { currentModLevel = it },
                valueRange = 0f..1f,
                steps = 4,
                modifier = Modifier.fillMaxWidth().testTag("moderation_slider")
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Null (None)", style = MaterialTheme.typography.bodySmall)
                Text("Maximum", style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Voice Cloning (ElevenLabs)",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Use an ElevenLabs API key and a cloned Voice ID to enable real voice synthesis. Add your voice profiles below to configure custom voice synthesis.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = currentElevenLabsKey,
                onValueChange = { currentElevenLabsKey = it },
                label = { Text("ElevenLabs API Key") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            Text("Your Voice Profiles", style = MaterialTheme.typography.titleMedium)
            if (voiceProfiles.isEmpty()) {
                Text("No voice profiles saved.", style = MaterialTheme.typography.bodySmall)
            } else {
                voiceProfiles.forEach { profile ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Column {
                            Text(profile.name, style = MaterialTheme.typography.bodyMedium)
                            Text(profile.voiceId, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Row {
                            if (!profile.isPrimary) {
                                TextButton(onClick = { 
                                    viewModel.setPrimaryVoiceProfile(profile)
                                    currentVoiceId = profile.voiceId
                                }) {
                                    Text("Set Primary")
                                }
                            } else {
                                Text("Primary", color = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(onClick = { viewModel.deleteVoiceProfile(profile) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete")
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text("Add New Voice Profile", style = MaterialTheme.typography.titleSmall)
            OutlinedTextField(
                value = newProfileName,
                onValueChange = { newProfileName = it },
                label = { Text("Profile Name (e.g. My Voice)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = newProfileVoiceId,
                onValueChange = { newProfileVoiceId = it },
                label = { Text("ElevenLabs Voice ID") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Button(
                onClick = {
                    if (newProfileName.isNotBlank() && newProfileVoiceId.isNotBlank()) {
                        viewModel.addVoiceProfile(newProfileName, newProfileVoiceId)
                        newProfileName = ""
                        newProfileVoiceId = ""
                    }
                },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Save Voice Profile")
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "AI Receptionist",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Configure your personal AI Receptionist to answer incoming calls.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text("Activate Receptionist Service")
                Switch(
                    checked = currentReceptionistActive,
                    onCheckedChange = { currentReceptionistActive = it }
                )
            }

            if (currentReceptionistActive) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Text("Auto-Answer Incoming Calls")
                    Switch(
                        checked = currentAutoAnswer,
                        onCheckedChange = { currentAutoAnswer = it }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = currentGreeting,
                    onValueChange = { currentGreeting = it },
                    label = { Text("Initial Greeting") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }

            Spacer(modifier = Modifier.height(48.dp))
            
            Button(
                onClick = {
                    coroutineScope.launch {
                        viewModel.saveSettings(
                            currentKey, 
                            currentModLevel, 
                            currentElevenLabsKey, 
                            currentVoiceId,
                            currentReceptionistActive,
                            currentAutoAnswer,
                            currentGreeting
                        )
                        onNavigateBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("save_settings_button")
            ) {
                Text("Save Configuration")
            }
        }
    }
}
