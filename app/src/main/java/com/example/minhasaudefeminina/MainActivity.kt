package com.example.minhasaudefeminina

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.minhasaudefeminina.app.AppContainer
import com.example.minhasaudefeminina.navigation.AppNavigation
import com.example.minhasaudefeminina.ui.screens.LoginScreen
import com.example.minhasaudefeminina.ui.theme.BackgroundFeminino
import com.example.minhasaudefeminina.ui.theme.MinhaSaudeFemininaTheme
import com.example.minhasaudefeminina.ui.theme.RosaPrimario
import com.example.minhasaudefeminina.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as MinhaSaudeFemininaApplication).container
        setContent {
            MinhaSaudeFemininaTheme {
                AppRoot(container)
            }
        }
    }
}

@Composable
private fun AppRoot(container: AppContainer) {
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.factory(container.authRepository))
    val state by authViewModel.uiState.collectAsStateWithLifecycle()

    when {
        state.isInitializing -> Box(
            modifier = Modifier.fillMaxSize().background(BackgroundFeminino),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = RosaPrimario)
        }
        state.user == null -> LoginScreen(viewModel = authViewModel)
        else -> key(state.user!!.id) {
            AppNavigation(
                container = container,
                user = state.user!!,
                authViewModel = authViewModel
            )
        }
    }
}
