package com.fortunesai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fortunesai.ui.ChatScreen
import com.fortunesai.ui.MainViewModel
import com.fortunesai.ui.SettingsScreen
import com.fortunesai.ui.TermsScreen
import com.fortunesai.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(darkTheme = true) { // Forced dark theme
                Surface(modifier = Modifier.fillMaxSize()) {
                    val hasAcceptedTerms by viewModel.hasAcceptedTerms.collectAsState()
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = if (hasAcceptedTerms) "chat" else "terms"
                    ) {
                        composable("terms") {
                            TermsScreen(
                                onAccepted = {
                                    navController.navigate("chat") {
                                        popUpTo("terms") { inclusive = true }
                                    }
                                },
                                viewModel = viewModel
                            )
                        }
                        composable("chat") {
                            ChatScreen(
                                onNavigateSettings = { navController.navigate("settings") },
                                viewModel = viewModel
                            )
                        }
                        composable("settings") {
                            SettingsScreen(
                                onNavigateBack = { navController.popBackStack() },
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
        }
    }
}
