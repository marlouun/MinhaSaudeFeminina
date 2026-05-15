package com.example.minhasaudefeminina

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.minhasaudefeminina.ui.screens.*
import com.example.minhasaudefeminina.ui.theme.BackgroundFeminino
import com.example.minhasaudefeminina.ui.theme.MinhaSaudeFemininaTheme
import com.example.minhasaudefeminina.ui.theme.RosaPrimario
import com.example.minhasaudefeminina.ui.theme.RosaSecundario
import com.example.minhasaudefeminina.viewmodel.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "Iniciando App...")
        enableEdgeToEdge()
        try {
            setContent {
                MinhaSaudeFemininaTheme {
                    MainContainer()
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Erro fatal no setContent: ${e.message}")
        }
    }
}

@Composable
fun MainContainer() {
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.authState.collectAsState()

    when (val state = authState) {
        is AuthState.Authenticated -> {
            MinhaSaudeFemininaApp()
        }
        is AuthState.Loading -> {
            Box(modifier = Modifier.fillMaxSize().background(BackgroundFeminino), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = RosaPrimario)
            }
        }
        else -> {
            LoginScreen(viewModel = authViewModel)
        }
    }
}

@Composable
fun MinhaSaudeFemininaApp() {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }
    
    // Lazy loading dos viewmodels para não travar no start
    val sintomasViewModel: SintomasViewModel = viewModel()
    val chatViewModel: ChatViewModel = viewModel()
    val perfilViewModel: PerfilViewModel = viewModel()

    Scaffold(
        bottomBar = {
            Column {
                CustomBottomBar(currentDestination) {
                    currentDestination = it
                }
                Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { currentDestination = AppDestinations.REGISTER },
                shape = CircleShape,
                containerColor = RosaSecundario,
                contentColor = Color.White,
                modifier = Modifier.offset(y = 55.dp),
                elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Registrar")
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (currentDestination) {
                AppDestinations.HOME -> HomeScreen(viewModel = sintomasViewModel)
                AppDestinations.DUVIDAS -> ChatScreen(viewModel = chatViewModel)
                AppDestinations.CONTEUDO -> EducacaoScreen()
                AppDestinations.PERFIL -> PerfilScreen(viewModel = perfilViewModel)
                AppDestinations.REGISTER -> RegistrarSintomaScreen(viewModel = sintomasViewModel)
            }
        }
    }
}

@Composable
fun CustomBottomBar(
    currentDestination: AppDestinations,
    onDestinationSelected: (AppDestinations) -> Unit
) {
    Surface(
        color = Color.White,
        tonalElevation = 0.dp,
        modifier = Modifier.height(70.dp).fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val items = AppDestinations.entries.filter { it != AppDestinations.REGISTER }
            
            // Lado Esquerdo
            items.take(2).forEach { destination ->
                BottomBarItem(destination, currentDestination == destination) {
                    onDestinationSelected(destination)
                }
            }
            
            Spacer(modifier = Modifier.width(60.dp)) // Espaço para o FAB central
            
            // Lado Direito
            items.drop(2).forEach { destination ->
                BottomBarItem(destination, currentDestination == destination) {
                    onDestinationSelected(destination)
                }
            }
        }
    }
}

@Composable
fun BottomBarItem(
    destination: AppDestinations,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Icon(
            imageVector = destination.icon,
            contentDescription = destination.label,
            tint = if (isSelected) RosaPrimario else Color.Gray,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = destination.label,
            fontSize = 10.sp,
            color = if (isSelected) RosaPrimario else Color.Gray
        )
    }
}

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
) {
    HOME("Hoje", Icons.Outlined.CalendarToday),
    DUVIDAS("Dúvidas", Icons.Outlined.ChatBubbleOutline),
    CONTEUDO("Conteudos", Icons.Outlined.Lightbulb),
    PERFIL("Perfil", Icons.Outlined.PersonOutline),
    REGISTER("Registrar", Icons.Default.Add)
}
