package com.example.minhasaudefeminina.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.minhasaudefeminina.ui.theme.*
import com.example.minhasaudefeminina.viewmodel.AuthState
import com.example.minhasaudefeminina.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MinhaContaScreen(viewModel: AuthViewModel, onVoltar: () -> Unit) {
    val authState by viewModel.authState.collectAsState()
    val user = remember { viewModel.authState.value as? AuthState.Authenticated }
    
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmNewPassword by remember { mutableStateOf("") }
    
    var showCurrentPass by remember { mutableStateOf(false) }
    var showNewPass by remember { mutableStateOf(false) }
    
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(authState) {
        if (authState is AuthState.PasswordChanged) {
            snackbarHostState.showSnackbar("Senha alterada com sucesso!")
            currentPassword = ""
            newPassword = ""
            confirmNewPassword = ""
            viewModel.resetState()
        } else if (authState is AuthState.Error) {
            snackbarHostState.showSnackbar((authState as AuthState.Error).message)
            viewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Minha Conta", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = LightPinkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(15.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Email Cadastrado", fontSize = 14.sp, color = Color.Gray)
                    Text(
                        text = user?.user?.email ?: "Não identificado",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = RosaPrimario
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Alterar Senha",
                modifier = Modifier.align(Alignment.Start),
                fontWeight = FontWeight.Bold,
                color = PurpleSelected
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = currentPassword,
                onValueChange = { currentPassword = it },
                label = { Text("Senha Atual") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (showCurrentPass) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showCurrentPass = !showCurrentPass }) {
                        Icon(if (showCurrentPass) Icons.Default.Visibility else Icons.Default.VisibilityOff, null)
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RosaPrimario)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text("Nova Senha (min. 6 carac.)") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (showNewPass) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showNewPass = !showNewPass }) {
                        Icon(if (showNewPass) Icons.Default.Visibility else Icons.Default.VisibilityOff, null)
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RosaPrimario)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = confirmNewPassword,
                onValueChange = { confirmNewPassword = it },
                label = { Text("Confirmar Nova Senha") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RosaPrimario)
            )

            Spacer(modifier = Modifier.height(32.dp))

            val isLoading = authState is AuthState.Loading
            Button(
                onClick = {
                    if (newPassword == confirmNewPassword && newPassword.length >= 6) {
                        viewModel.changePassword(currentPassword, newPassword)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !isLoading && currentPassword.isNotEmpty() && newPassword.isNotEmpty() && newPassword == confirmNewPassword,
                colors = ButtonDefaults.buttonColors(containerColor = RosaPrimario),
                shape = RoundedCornerShape(15.dp)
            ) {
                if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                else Text("Atualizar Senha", fontWeight = FontWeight.Bold)
            }
        }
    }
}
