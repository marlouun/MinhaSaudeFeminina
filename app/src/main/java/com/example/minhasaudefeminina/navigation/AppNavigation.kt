package com.example.minhasaudefeminina.navigation

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.minhasaudefeminina.app.AppContainer
import com.example.minhasaudefeminina.model.Usuario
import com.example.minhasaudefeminina.ui.screens.ArticleDetailScreen
import com.example.minhasaudefeminina.ui.screens.ChatScreen
import com.example.minhasaudefeminina.ui.screens.EducacaoScreen
import com.example.minhasaudefeminina.ui.screens.HomeScreen
import com.example.minhasaudefeminina.ui.screens.MinhaContaScreen
import com.example.minhasaudefeminina.ui.screens.PerfilScreen
import com.example.minhasaudefeminina.ui.screens.RegistrarSintomaScreen
import com.example.minhasaudefeminina.ui.screens.ViolentometroScreen
import com.example.minhasaudefeminina.ui.theme.RosaPrimario
import com.example.minhasaudefeminina.ui.theme.RosaSecundario
import com.example.minhasaudefeminina.viewmodel.ArticleViewModel
import com.example.minhasaudefeminina.viewmodel.AuthViewModel
import com.example.minhasaudefeminina.viewmodel.ChatViewModel
import com.example.minhasaudefeminina.viewmodel.PerfilViewModel
import com.example.minhasaudefeminina.viewmodel.SintomasViewModel
import java.time.LocalDate

private object Routes {
    const val HOME = "home"
    const val CHAT = "chat"
    const val CONTENT = "content"
    const val PROFILE = "profile"
    const val ACCOUNT = "account"
    const val VIOLENTOMETER = "violentometer"
    const val SYMPTOM_PATTERN = "symptom/{date}/{recordId}"
    const val ARTICLE_PATTERN = "article/{articleId}"

    fun symptom(date: LocalDate, recordId: String? = null): String =
        "symptom/${date.toEpochDay()}/${Uri.encode(recordId ?: "new")}" 

    fun article(articleId: String): String = "article/${Uri.encode(articleId)}"
}

private data class RootDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private val rootDestinations = listOf(
    RootDestination(Routes.HOME, "Hoje", Icons.Outlined.CalendarToday),
    RootDestination(Routes.CHAT, "Dúvidas", Icons.Outlined.ChatBubbleOutline),
    RootDestination(Routes.CONTENT, "Conteúdos", Icons.Outlined.Lightbulb),
    RootDestination(Routes.PROFILE, "Perfil", Icons.Outlined.PersonOutline)
)

@Composable
fun AppNavigation(
    container: AppContainer,
    user: Usuario,
    authViewModel: AuthViewModel
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showMainChrome = rootDestinations.any { it.route == currentRoute }

    val symptomViewModel: SintomasViewModel = viewModel(
        key = "symptoms-${user.id}",
        factory = SintomasViewModel.factory(container.symptomRepository, user.id)
    )
    val chatViewModel: ChatViewModel = viewModel(
        key = "chat-${user.id}",
        factory = ChatViewModel.factory(container.chatRepository, user.id)
    )
    val profileViewModel: PerfilViewModel = viewModel(
        key = "profile-${user.id}",
        factory = PerfilViewModel.factory(container.profileRepository, container.symptomRepository, user.id)
    )
    val articleViewModel: ArticleViewModel = viewModel(
        key = "articles",
        factory = ArticleViewModel.factory(container.articleRepository)
    )

    fun navigateRoot(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        bottomBar = {
            if (showMainChrome) {
                MainBottomBar(currentRoute = currentRoute, onSelect = ::navigateRoot)
            }
        },
        floatingActionButton = {
            if (showMainChrome) {
                FloatingActionButton(
                    onClick = { navController.navigate(Routes.symptom(LocalDate.now())) },
                    shape = CircleShape,
                    containerColor = RosaSecundario,
                    contentColor = Color.White,
                    elevation = FloatingActionButtonDefaults.elevation(2.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Registrar sintoma")
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    viewModel = symptomViewModel,
                    onAddRecord = { date -> navController.navigate(Routes.symptom(date)) },
                    onEditRecord = { record -> navController.navigate(Routes.symptom(record.localDate(), record.id)) }
                )
            }
            composable(Routes.CHAT) { ChatScreen(viewModel = chatViewModel) }
            composable(Routes.CONTENT) {
                EducacaoScreen(
                    viewModel = articleViewModel,
                    onOpenArticle = { navController.navigate(Routes.article(it)) }
                )
            }
            composable(Routes.PROFILE) {
                PerfilScreen(
                    viewModel = profileViewModel,
                    user = user,
                    onOpenAccount = { navController.navigate(Routes.ACCOUNT) },
                    onOpenViolentometer = { navController.navigate(Routes.VIOLENTOMETER) },
                    onLogout = authViewModel::logout
                )
            }
            composable(Routes.ACCOUNT) {
                MinhaContaScreen(viewModel = authViewModel, onVoltar = navController::popBackStack)
            }
            composable(Routes.VIOLENTOMETER) {
                ViolentometroScreen(onVoltar = navController::popBackStack)
            }
            composable(
                route = Routes.SYMPTOM_PATTERN,
                arguments = listOf(
                    navArgument("date") { type = NavType.LongType },
                    navArgument("recordId") { type = NavType.StringType }
                )
            ) { entry ->
                val date = LocalDate.ofEpochDay(entry.arguments?.getLong("date") ?: LocalDate.now().toEpochDay())
                val recordId = entry.arguments?.getString("recordId")?.takeUnless { it == "new" }
                RegistrarSintomaScreen(
                    viewModel = symptomViewModel,
                    initialDate = date,
                    recordId = recordId,
                    onVoltar = navController::popBackStack,
                    onFinished = navController::popBackStack
                )
            }
            composable(
                route = Routes.ARTICLE_PATTERN,
                arguments = listOf(navArgument("articleId") { type = NavType.StringType })
            ) { entry ->
                ArticleDetailScreen(
                    viewModel = articleViewModel,
                    articleId = entry.arguments?.getString("articleId").orEmpty(),
                    onVoltar = navController::popBackStack
                )
            }
        }
    }
}

@Composable
private fun MainBottomBar(currentRoute: String?, onSelect: (String) -> Unit) {
    Surface(color = Color.White, shadowElevation = 6.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().height(72.dp).padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            rootDestinations.take(2).forEach { destination ->
                BottomItem(destination, destination.route == currentRoute) { onSelect(destination.route) }
            }
            Spacer(Modifier.width(60.dp))
            rootDestinations.drop(2).forEach { destination ->
                BottomItem(destination, destination.route == currentRoute) { onSelect(destination.route) }
            }
        }
    }
}

@Composable
private fun BottomItem(destination: RootDestination, selected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick).padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = destination.icon,
            contentDescription = destination.label,
            tint = if (selected) RosaPrimario else Color.Gray,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = destination.label,
            color = if (selected) RosaPrimario else Color.Gray,
            fontSize = 11.sp
        )
    }
}
