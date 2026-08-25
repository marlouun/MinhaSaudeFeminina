package com.example.minhasaudefeminina.data.repository

import com.example.minhasaudefeminina.model.Artigo
import com.example.minhasaudefeminina.model.MensagemChat
import com.example.minhasaudefeminina.model.PerfilUsuario
import com.example.minhasaudefeminina.model.RegistroSintoma
import com.example.minhasaudefeminina.model.Usuario
import kotlinx.coroutines.flow.Flow

open class RepositoryException(message: String, cause: Throwable? = null) : Exception(message, cause)
class ValidationException(message: String) : RepositoryException(message)
class AuthenticationException(message: String) : RepositoryException(message)
class NotFoundException(message: String) : RepositoryException(message)

interface AuthRepository {
    val sessionUser: Flow<Usuario?>

    suspend fun createAccount(name: String, email: String, password: CharArray): Usuario
    suspend fun login(email: String, password: CharArray): Usuario
    suspend fun logout()
    suspend fun updateAccount(userId: String, name: String, email: String): Usuario
    suspend fun changePassword(userId: String, currentPassword: CharArray, newPassword: CharArray)
    suspend fun deleteAccount(userId: String, password: CharArray)
}

interface ProfileRepository {
    fun observeProfile(userId: String): Flow<PerfilUsuario>
    suspend fun saveProfile(profile: PerfilUsuario)
}

interface SymptomRepository {
    fun observeRecords(userId: String): Flow<List<RegistroSintoma>>
    suspend fun getRecord(userId: String, recordId: String): RegistroSintoma?
    suspend fun saveRecord(record: RegistroSintoma)
    suspend fun deleteRecord(userId: String, recordId: String)
}

interface ArticleRepository {
    fun observePublishedArticles(): Flow<List<Artigo>>
    suspend fun getArticle(articleId: String): Artigo?
    suspend fun seedIfEmpty()
}

interface ChatRepository {
    fun observeMessages(userId: String): Flow<List<MensagemChat>>
    suspend fun ensureGreeting(userId: String, sessionId: String)
    suspend fun saveMessage(message: MensagemChat)
    suspend fun clearHistory(userId: String)
}
