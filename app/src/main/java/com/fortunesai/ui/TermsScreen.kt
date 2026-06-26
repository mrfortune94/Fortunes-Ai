package com.fortunesai.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import kotlinx.coroutines.launch

@Composable
fun TermsScreen(
    onAccepted: () -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    var checked by remember { mutableStateOf(false) }
    val hasAccepted by viewModel.hasAcceptedTerms.collectAsState()

    LaunchedEffect(hasAccepted) {
        if (hasAccepted) {
            onAccepted()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.AccountBalance,
            contentDescription = "Terms Icon",
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Terms and Conditions",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        Box(
            modifier = Modifier
                .weight(1f)
                .background(MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.medium)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Welcome to FortunesAi.\n\nBy using this multi-modal AI chat application, you agree to the following:\n\n" +
                        "1. You are solely responsible for the API keys you input into the application.\n" +
                        "2. All generation costs incurred by using the provided AI models are your responsibility.\n" +
                        "3. You will not use the app to generate harmful, illegal, or malicious content.\n" +
                        "4. Your API keys are stored locally on your device and are never transmitted to our servers.\n\n" +
                        "Please acknowledge these terms to proceed.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = { checked = it },
                modifier = Modifier.testTag("terms_checkbox")
            )
            Text(
                text = "I have read and agree to the Terms.",
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = {
                coroutineScope.launch {
                    viewModel.acceptTerms()
                    onAccepted()
                }
            },
            enabled = checked,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("accept_terms_button")
        ) {
            Text("Accept & Continue")
        }
    }
}
