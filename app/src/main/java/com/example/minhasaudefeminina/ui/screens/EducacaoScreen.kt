package com.example.minhasaudefeminina.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.minhasaudefeminina.model.Artigo
import com.example.minhasaudefeminina.ui.components.ArticleContentRenderer
import com.example.minhasaudefeminina.ui.theme.LightPinkBackground
import com.example.minhasaudefeminina.ui.theme.RosaClaro
import com.example.minhasaudefeminina.ui.theme.RosaPrimario
import com.example.minhasaudefeminina.ui.theme.RosaSecundario
import com.example.minhasaudefeminina.viewmodel.ArticleViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EducacaoScreen(viewModel: ArticleViewModel, onOpenArticle: (String) -> Unit) {
    val articles by viewModel.filteredArticles.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.category.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize().background(LightPinkBackground)) {
        Surface(modifier = Modifier.fillMaxWidth(), color = Color.White, shadowElevation = 2.dp) {
            Text(
                "♀ Minha Saúde Feminina",
                modifier = Modifier.padding(16.dp),
                color = RosaPrimario,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(Color(0xFF7B42F5), RosaSecundario)))
                    .padding(22.dp)
            ) {
                Column {
                    Text("Educação em saúde", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text("Conteúdos locais para apoiar sua conversa com a UBS", color = Color.White.copy(alpha = 0.9f))
                }
            }
        }

        OutlinedTextField(
            value = query,
            onValueChange = viewModel::setQuery,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            placeholder = { Text("Pesquisar artigos") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setQuery("") }) { Icon(Icons.Default.Clear, "Limpar pesquisa") }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = RosaPrimario,
                unfocusedBorderColor = RosaClaro,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { viewModel.setCategory(null) },
                    label = { Text("Todos") },
                    colors = articleChipColors()
                )
            }
            items(categories) { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { viewModel.setCategory(category) },
                    label = { Text(category) },
                    colors = articleChipColors()
                )
            }
        }

        if (articles.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Nenhum artigo encontrado.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, top = 4.dp, end = 16.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(articles, key = Artigo::id) { article ->
                    ArticleCard(article) { onOpenArticle(article.id) }
                }
            }
        }
    }
}

@Composable
private fun ArticleCard(article: Artigo, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(9.dp).background(RosaSecundario, CircleShape))
                Text(article.categoria, modifier = Modifier.padding(start = 8.dp), fontSize = 12.sp, color = RosaSecundario, fontWeight = FontWeight.Bold)
            }
            Text(article.titulo, modifier = Modifier.padding(top = 8.dp), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            if (article.subtitulo.isNotBlank()) {
                Text(article.subtitulo, modifier = Modifier.padding(top = 4.dp), color = Color.DarkGray, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Text(article.resumo, modifier = Modifier.padding(top = 8.dp), fontSize = 13.sp, color = Color.Gray, maxLines = 3, overflow = TextOverflow.Ellipsis)
            Text("Ler artigo →", modifier = Modifier.padding(top = 12.dp), color = RosaPrimario, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ArticleDetailScreen(viewModel: ArticleViewModel, articleId: String, onVoltar: () -> Unit) {
    val article by viewModel.selectedArticle.collectAsStateWithLifecycle()

    LaunchedEffect(articleId) {
        viewModel.clearSelectedArticle()
        viewModel.loadArticle(articleId)
    }
    DisposableEffect(Unit) {
        onDispose(viewModel::clearSelectedArticle)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(article?.categoria ?: "Artigo") },
                navigationIcon = {
                    IconButton(onClick = onVoltar) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { padding ->
        if (article == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = RosaPrimario)
            }
        } else {
            val current = article!!
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                current.capaUri?.let { cover ->
                    item {
                        AsyncImage(model = cover, contentDescription = "Imagem de capa", modifier = Modifier.fillMaxWidth())
                    }
                }
                item {
                    Icon(Icons.Default.Favorite, null, tint = RosaSecundario, modifier = Modifier.size(40.dp))
                    Text(current.titulo, fontSize = 27.sp, lineHeight = 33.sp, fontWeight = FontWeight.ExtraBold)
                    if (current.subtitulo.isNotBlank()) {
                        Text(current.subtitulo, modifier = Modifier.padding(top = 8.dp), fontSize = 17.sp, lineHeight = 24.sp, color = Color.DarkGray)
                    }
                    Text(
                        "${current.autor} • ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(current.atualizadoEm))}",
                        modifier = Modifier.padding(top = 12.dp),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                item { ArticleContentRenderer(current.conteudoJson, Modifier.fillMaxWidth()) }
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = RosaClaro.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "Conteúdo informativo. Para diagnóstico, tratamento ou urgência, procure um serviço de saúde.",
                            modifier = Modifier.padding(14.dp),
                            fontSize = 12.sp,
                            color = Color.DarkGray
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun articleChipColors() = FilterChipDefaults.filterChipColors(
    selectedContainerColor = RosaSecundario,
    selectedLabelColor = Color.White
)
