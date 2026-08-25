package com.example.minhasaudefeminina.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.minhasaudefeminina.data.repository.ArticleRepository
import com.example.minhasaudefeminina.model.Artigo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ArticleViewModel(private val repository: ArticleRepository) : ViewModel() {
    private val articles = repository.observePublishedArticles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _category = MutableStateFlow<String?>(null)
    val category: StateFlow<String?> = _category.asStateFlow()

    private val _selectedArticle = MutableStateFlow<Artigo?>(null)
    val selectedArticle: StateFlow<Artigo?> = _selectedArticle.asStateFlow()

    val categories: StateFlow<List<String>> = articles
        .map { list -> list.map(Artigo::categoria).distinct().sorted() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val filteredArticles: StateFlow<List<Artigo>> = combine(articles, _query, _category) { list, query, category ->
        list.filter { article ->
            val matchesCategory = category == null || article.categoria == category
            val term = query.trim()
            val matchesQuery = term.isEmpty() || listOf(
                article.titulo,
                article.subtitulo,
                article.resumo,
                article.tags.joinToString(" ")
            ).any { it.contains(term, ignoreCase = true) }
            matchesCategory && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setQuery(value: String) {
        _query.value = value.take(100)
    }

    fun setCategory(value: String?) {
        _category.value = value
    }

    fun loadArticle(articleId: String) {
        viewModelScope.launch { _selectedArticle.value = repository.getArticle(articleId) }
    }

    fun clearSelectedArticle() {
        _selectedArticle.value = null
    }

    companion object {
        fun factory(repository: ArticleRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { ArticleViewModel(repository) }
        }
    }
}
