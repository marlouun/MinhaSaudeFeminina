package com.example.minhasaudefeminina

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.minhasaudefeminina.ui.screens.HomeScreen
import com.example.minhasaudefeminina.ui.screens.RegistrarSintomaScreen
import com.example.minhasaudefeminina.ui.theme.MinhaSaudeFemininaTheme
import com.example.minhasaudefeminina.ui.theme.RosaPrimario
import com.example.minhasaudefeminina.ui.theme.RosaSecundario
import com.example.minhasaudefeminina.viewmodel.SintomasViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MinhaSaudeFemininaTheme {
                MinhaSaudeFemininaApp()
            }
        }
    }
}

@Composable
fun MinhaSaudeFemininaApp() {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }
    val sintomasViewModel: SintomasViewModel = viewModel()

    Scaffold(
        bottomBar = {
            CustomBottomBar(currentDestination) {
                currentDestination = it
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { currentDestination = AppDestinations.REGISTER },
                shape = CircleShape,
                containerColor = RosaSecundario,
                contentColor = Color.White,
                modifier = Modifier.offset(y = 50.dp), // Push FAB down into the bottom bar gap
            ) {
                Icon(Icons.Default.Add, contentDescription = "Registrar")
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (currentDestination) {
                AppDestinations.HOME -> HomeScreen()
                AppDestinations.REGISTER -> RegistrarSintomaScreen(viewModel = sintomasViewModel)
                else -> {
                    Text(
                        text = "Tela em desenvolvimento",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Composable
fun CustomBottomBar(
    currentDestination: AppDestinations,
    onDestinationSelected: (AppDestinations) -> Unit
) {
    BottomAppBar(
        containerColor = Color.White,
        tonalElevation = 8.dp,
        modifier = Modifier.height(70.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppDestinations.entries.take(2).forEach { destination ->
                BottomBarItem(destination, currentDestination == destination) {
                    onDestinationSelected(destination)
                }
            }
            
            Spacer(modifier = Modifier.width(60.dp)) // Space for FAB
            
            AppDestinations.entries.drop(2).forEach { destination ->
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
    HOME("Hoje", Icons.Default.Home),
    CICLO("Ciclo", Icons.Default.CalendarMonth),
    CONTEUDO("Conteudos", Icons.Default.Lightbulb),
    PERFIL("Perfil", Icons.Default.Person),
    REGISTER("Registrar", Icons.Default.Add)
}
