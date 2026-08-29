package com.example.minhasaudefeminina.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.minhasaudefeminina.domain.validation.InputValidators
import com.example.minhasaudefeminina.ui.theme.LightPinkBackground
import com.example.minhasaudefeminina.ui.theme.RedSintoma
import com.example.minhasaudefeminina.ui.theme.RosaClaro
import com.example.minhasaudefeminina.ui.theme.RosaPrimario
import com.example.minhasaudefeminina.viewmodel.AuthEvent
import com.example.minhasaudefeminina.viewmodel.AuthViewModel

@Composable
fun MinhaContaScreen(viewModel: AuthViewModel, onVoltar: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val user = state.user
    val snackbar = remember { SnackbarHostState() }

    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var currentPassword by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var showCurrent by rememberSaveable { mutableStateOf(false) }
    var showNew by rememberSaveable { mutableStateOf(false) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    var deletePassword by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(user?.id, user?.atualizadoEm) {
        user?.let {
            name = it.nome
            email = it.email
        }
    }
    LaunchedEffect(state.message) {
        state.message?.let {
            snackbar.showSnackbar(it)
            viewModel.consumeMessage()
        }
    }
    LaunchedEffect(state.event) {
        when (state.event) {
            AuthEvent.ACCOUNT_UPDATED -> snackbar.showSnackbar("Dados da conta atualizados.")
            AuthEvent.PASSWORD_CHANGED -> {
                snackbar.showSnackbar("Senha alterada com sucesso.")
                currentPassword = ""
                newPassword = ""
                confirmPassword = ""
            }
            AuthEvent.ACCOUNT_DELETED -> Unit
            null -> Unit
        }
        if (state.event != null) viewModel.consumeEvent()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Minha conta", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onVoltar) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = LightPinkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            AccountCard("Dados pessoais") {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(80) },
                    label = { Text("Nome") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = InputValidators.nameError(name) != null,
                    colors = accountFieldColors()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it.take(254) },
                    label = { Text("E-mail") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    isError = InputValidators.emailError(email) != null,
                    colors = accountFieldColors()
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.updateAccount(name, email) },
                    enabled = !state.isLoading && InputValidators.nameError(name) == null && InputValidators.emailError(email) == null,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = RosaPrimario)
                ) {
                    if (state.isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.height(20.dp))
                    else Text("Salvar dados")
                }
            }

            Spacer(Modifier.height(16.dp))

            AccountCard("Alterar senha") {
                PasswordField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it.take(128) },
                    label = "Senha atual",
                    visible = showCurrent,
                    onToggle = { showCurrent = !showCurrent }
                )
                Spacer(Modifier.height(12.dp))
                PasswordField(
                    value = newPassword,
                    onValueChange = { newPassword = it.take(128) },
                    label = "Nova senha",
                    visible = showNew,
                    onToggle = { showNew = !showNew }
                )
                Spacer(Modifier.height(12.dp))
                PasswordField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it.take(128) },
                    label = "Confirmar nova senha",
                    visible = false,
                    onToggle = null
                )
                val passwordValid = InputValidators.passwordError(newPassword) == null && newPassword == confirmPassword
                if (newPassword.isNotEmpty() && InputValidators.passwordError(newPassword) != null) {
                    Text(InputValidators.passwordError(newPassword).orEmpty(), color = RedSintoma, modifier = Modifier.padding(top = 6.dp))
                } else if (confirmPassword.isNotEmpty() && newPassword != confirmPassword) {
                    Text("As senhas não conferem.", color = RedSintoma, modifier = Modifier.padding(top = 6.dp))
                }
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.changePassword(currentPassword, newPassword) },
                    enabled = !state.isLoading && currentPassword.isNotBlank() && passwordValid,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = RosaPrimario)
                ) { Text("Atualizar senha") }
            }

            Spacer(Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(15.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Excluir conta local", fontWeight = FontWeight.Bold, color = RedSintoma)
                    Text(
                        "A conta, o perfil, os sintomas e a conversa serão removidos deste aparelho. Esta ação não pode ser desfeita.",
                        modifier = Modifier.padding(top = 6.dp, bottom = 12.dp),
                        color = Color.DarkGray
                    )
                    Button(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = RedSintoma)
                    ) {
                        Icon(Icons.Default.DeleteForever, null)
                        Text("Excluir minha conta", modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
            Spacer(Modifier.height(30.dp))
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                deletePassword = ""
            },
            title = { Text("Confirmar exclusão") },
            text = {
                Column {
                    Text("Digite sua senha para excluir definitivamente os dados locais.")
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = deletePassword,
                        onValueChange = { deletePassword = it.take(128) },
                        label = { Text("Senha") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteAccount(deletePassword)
                        deletePassword = ""
                    },
                    enabled = deletePassword.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = RedSintoma)
                ) { Text("Excluir") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    deletePassword = ""
                }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun AccountCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(15.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, color = RosaPrimario)
            Spacer(Modifier.height(14.dp))
            content()
        }
    }
}

@Composable
private fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    visible: Boolean,
    onToggle: (() -> Unit)?
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = onToggle?.let {
            {
                IconButton(onClick = it) {
                    Icon(if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                }
            }
        },
        colors = accountFieldColors()
    )
}

@Composable
private fun accountFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = RosaPrimario,
    unfocusedBorderColor = RosaClaro
)
