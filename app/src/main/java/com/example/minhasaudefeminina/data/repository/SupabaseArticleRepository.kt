package com.example.minhasaudefeminina.data.repository

import com.example.minhasaudefeminina.data.local.AppDatabase
import com.example.minhasaudefeminina.data.local.ArticleEntity
import com.example.minhasaudefeminina.data.remote.SupabaseConfig
import com.example.minhasaudefeminina.model.Artigo
import com.example.minhasaudefeminina.model.ArtigoStatus
import java.net.HttpURLConnection
import java.net.URL
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class SupabaseArticleRepository(private val database: AppDatabase) : ArticleRepository {
    private val dao = database.articleDao()

    override fun observePublishedArticles(): Flow<List<Artigo>> = channelFlow {
        launch {
            dao.observePublished().collect { entities ->
                send(entities.map(ArticleEntity::toRemoteDomain))
            }
        }
        launch {
            while (isActive) {
                runCatching { syncPublishedArticles() }
                delay(30_000)
            }
        }
    }

    override suspend fun getArticle(articleId: String): Artigo? {
        dao.getById(articleId)?.let { return it.toRemoteDomain() }
        runCatching { syncPublishedArticles() }
        return dao.getById(articleId)?.toRemoteDomain()
    }

    override suspend fun seedIfEmpty() {
        if (dao.count() == 0) dao.upsertAll(ArticleSeedData.create())
        runCatching { syncPublishedArticles() }
    }

    private suspend fun syncPublishedArticles() = withContext(Dispatchers.IO) {
        val endpoint = "${SupabaseConfig.PROJECT_URL}/rest/v1/articles?select=*&status=eq.published&order=updated_at.desc"
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10_000
            readTimeout = 10_000
            setRequestProperty("apikey", SupabaseConfig.PUBLISHABLE_KEY)
            setRequestProperty("Accept", "application/json")
        }

        try {
            val code = connection.responseCode
            if (code !in 200..299) {
                val message = connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
                throw RepositoryException("Não foi possível atualizar os artigos ($code). $message")
            }
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val rows = JSONArray(body)
            val articles = buildList {
                for (index in 0 until rows.length()) add(rows.getJSONObject(index).toArticleEntity())
            }
            dao.deleteAll()
            if (articles.isNotEmpty()) dao.upsertAll(articles)
        } finally {
            connection.disconnect()
        }
    }
}

private fun JSONObject.toArticleEntity(): ArticleEntity {
    val updatedAt = optString("updated_at").toEpochMillis()
    val createdAt = optString("created_at").toEpochMillis()
    val tagsJson = optJSONArray("tags") ?: JSONArray()
    val tags = buildList {
        for (index in 0 until tagsJson.length()) add(tagsJson.optString(index))
    }.filter(String::isNotBlank)
    val content = when (val value = opt("content_json")) {
        is JSONObject -> value.toString()
        is JSONArray -> value.toString()
        is String -> value
        else -> "{\"type\":\"doc\",\"content\":[]}"
    }
    return ArticleEntity(
        id = getString("id"),
        slug = getString("slug"),
        category = optString("category"),
        title = getString("title"),
        subtitle = optString("subtitle"),
        summary = optString("summary"),
        contentJson = content,
        author = optString("author"),
        tagsCsv = tags.joinToString(","),
        coverUri = optString("cover_url").takeIf { it.isNotBlank() && it != "null" },
        status = ArtigoStatus.PUBLICADO.name,
        createdAt = createdAt,
        updatedAt = updatedAt,
        publishedAt = updatedAt
    )
}

private fun String.toEpochMillis(): Long = runCatching { Instant.parse(this).toEpochMilli() }
    .getOrDefault(System.currentTimeMillis())

private fun ArticleEntity.toRemoteDomain() = Artigo(
    id = id,
    slug = slug,
    categoria = category,
    titulo = title,
    subtitulo = subtitle,
    resumo = summary,
    conteudoJson = contentJson,
    autor = author,
    tags = tagsCsv.split(',').map(String::trim).filter(String::isNotEmpty),
    capaUri = coverUri,
    status = runCatching { ArtigoStatus.valueOf(status) }.getOrDefault(ArtigoStatus.RASCUNHO),
    criadoEm = createdAt,
    atualizadoEm = updatedAt,
    publicadoEm = publishedAt
)
