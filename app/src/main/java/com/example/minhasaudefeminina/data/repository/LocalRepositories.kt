package com.example.minhasaudefeminina.data.repository

import androidx.room.withTransaction
import com.example.minhasaudefeminina.data.local.AppDatabase
import com.example.minhasaudefeminina.data.local.ArticleEntity
import com.example.minhasaudefeminina.data.local.ChatMessageEntity
import com.example.minhasaudefeminina.data.local.ProfileEntity
import com.example.minhasaudefeminina.data.local.SessionPreferences
import com.example.minhasaudefeminina.data.local.SymptomRecordEntity
import com.example.minhasaudefeminina.data.local.UserEntity
import com.example.minhasaudefeminina.data.security.PasswordHasher
import com.example.minhasaudefeminina.domain.validation.InputValidators
import com.example.minhasaudefeminina.model.Artigo
import com.example.minhasaudefeminina.model.ArtigoStatus
import com.example.minhasaudefeminina.model.FaseVida
import com.example.minhasaudefeminina.model.MensagemChat
import com.example.minhasaudefeminina.model.PerfilUsuario
import com.example.minhasaudefeminina.model.RegistroSintoma
import com.example.minhasaudefeminina.model.SintomaTipo
import com.example.minhasaudefeminina.model.Usuario
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

class LocalAuthRepository(
    private val database: AppDatabase,
    private val sessionPreferences: SessionPreferences,
    private val passwordHasher: PasswordHasher = PasswordHasher()
) : AuthRepository {
    private val userDao = database.userDao()
    private val profileDao = database.profileDao()

    override val sessionUser: Flow<Usuario?> = sessionPreferences.currentUserId
        .distinctUntilChanged()
        .flatMapLatest { userId ->
            if (userId == null) {
                flowOf(null)
            } else {
                userDao.observeById(userId)
                    .onEach { if (it == null) sessionPreferences.clearCurrentUser() }
                    .map { it?.toDomain() }
            }
        }

    override suspend fun createAccount(name: String, email: String, password: CharArray): Usuario {
        try {
            InputValidators.nameError(name)?.let { throw ValidationException(it) }
            InputValidators.emailError(email)?.let { throw ValidationException(it) }
            InputValidators.passwordError(password.concatToString())?.let { throw ValidationException(it) }

            val normalizedEmail = email.normalizeEmail()
            if (userDao.getByEmail(normalizedEmail) != null) {
                throw ValidationException("Já existe uma conta com este e-mail.")
            }

            val now = System.currentTimeMillis()
            val userId = UUID.randomUUID().toString()
            val digest = passwordHasher.create(password)
            val entity = UserEntity(
                id = userId,
                displayName = name.trim(),
                email = email.trim(),
                emailNormalized = normalizedEmail,
                passwordHash = digest.hash,
                passwordSalt = digest.salt,
                passwordAlgorithm = digest.algorithm,
                passwordIterations = digest.iterations,
                createdAt = now,
                updatedAt = now
            )

            database.withTransaction {
                userDao.insert(entity)
                profileDao.upsert(
                    ProfileEntity(
                        userId = userId,
                        lifeStage = FaseVida.IDADE_REPRODUTIVA.name,
                        isPregnant = false,
                        papSmearDate = null,
                        mammogramDate = null,
                        photoUri = null
                    )
                )
            }
            sessionPreferences.setCurrentUser(userId)
            return entity.toDomain()
        } finally {
            password.fill('\u0000')
        }
    }

    override suspend fun login(email: String, password: CharArray): Usuario {
        try {
            InputValidators.emailError(email)?.let { throw AuthenticationException("E-mail ou senha incorretos.") }
            val entity = userDao.getByEmail(email.normalizeEmail())
                ?: throw AuthenticationException("E-mail ou senha incorretos.")
            val valid = passwordHasher.verify(
                password = password,
                expectedHash = entity.passwordHash,
                encodedSalt = entity.passwordSalt,
                algorithm = entity.passwordAlgorithm,
                iterations = entity.passwordIterations
            )
            if (!valid) throw AuthenticationException("E-mail ou senha incorretos.")
            sessionPreferences.setCurrentUser(entity.id)
            return entity.toDomain()
        } finally {
            password.fill('\u0000')
        }
    }

    override suspend fun logout() {
        sessionPreferences.clearCurrentUser()
    }

    override suspend fun updateAccount(userId: String, name: String, email: String): Usuario {
        InputValidators.nameError(name)?.let { throw ValidationException(it) }
        InputValidators.emailError(email)?.let { throw ValidationException(it) }
        val current = userDao.getById(userId) ?: throw NotFoundException("Conta local não encontrada.")
        val normalized = email.normalizeEmail()
        val emailOwner = userDao.getByEmail(normalized)
        if (emailOwner != null && emailOwner.id != userId) {
            throw ValidationException("Este e-mail já está sendo usado.")
        }
        val updated = current.copy(
            displayName = name.trim(),
            email = email.trim(),
            emailNormalized = normalized,
            updatedAt = System.currentTimeMillis()
        )
        userDao.update(updated)
        return updated.toDomain()
    }

    override suspend fun changePassword(
        userId: String,
        currentPassword: CharArray,
        newPassword: CharArray
    ) {
        try {
            InputValidators.passwordError(newPassword.concatToString())?.let { throw ValidationException(it) }
            val current = userDao.getById(userId) ?: throw NotFoundException("Conta local não encontrada.")
            val valid = passwordHasher.verify(
                currentPassword,
                current.passwordHash,
                current.passwordSalt,
                current.passwordAlgorithm,
                current.passwordIterations
            )
            if (!valid) throw AuthenticationException("A senha atual está incorreta.")
            val digest = passwordHasher.create(newPassword)
            userDao.update(
                current.copy(
                    passwordHash = digest.hash,
                    passwordSalt = digest.salt,
                    passwordAlgorithm = digest.algorithm,
                    passwordIterations = digest.iterations,
                    updatedAt = System.currentTimeMillis()
                )
            )
        } finally {
            currentPassword.fill('\u0000')
            newPassword.fill('\u0000')
        }
    }

    override suspend fun deleteAccount(userId: String, password: CharArray) {
        try {
            val current = userDao.getById(userId) ?: throw NotFoundException("Conta local não encontrada.")
            val valid = passwordHasher.verify(
                password,
                current.passwordHash,
                current.passwordSalt,
                current.passwordAlgorithm,
                current.passwordIterations
            )
            if (!valid) throw AuthenticationException("Senha incorreta. A conta não foi excluída.")
            database.withTransaction { userDao.deleteById(userId) }
            sessionPreferences.clearCurrentUser()
        } finally {
            password.fill('\u0000')
        }
    }

    private fun String.normalizeEmail(): String = trim().lowercase(Locale.ROOT)
}

class LocalProfileRepository(private val database: AppDatabase) : ProfileRepository {
    private val dao = database.profileDao()

    override fun observeProfile(userId: String): Flow<PerfilUsuario> = dao.observe(userId).map { entity ->
        entity?.toDomain() ?: PerfilUsuario(usuarioId = userId)
    }

    override suspend fun saveProfile(profile: PerfilUsuario) {
        dao.upsert(profile.toEntity())
    }
}

class LocalSymptomRepository(private val database: AppDatabase) : SymptomRepository {
    private val dao = database.symptomDao()

    override fun observeRecords(userId: String): Flow<List<RegistroSintoma>> =
        dao.observeForUser(userId).map { records -> records.map(SymptomRecordEntity::toDomain) }

    override suspend fun getRecord(userId: String, recordId: String): RegistroSintoma? =
        dao.getById(recordId, userId)?.toDomain()

    override suspend fun saveRecord(record: RegistroSintoma) {
        if (record.intensidade !in 1..5) throw ValidationException("A intensidade deve ficar entre 1 e 5.")
        InputValidators.notesError(record.notas.orEmpty())?.let { throw ValidationException(it) }
        dao.upsert(record.toEntity())
    }

    override suspend fun deleteRecord(userId: String, recordId: String) {
        dao.deleteById(recordId, userId)
    }
}

class LocalArticleRepository(private val database: AppDatabase) : ArticleRepository {
    private val dao = database.articleDao()

    override fun observePublishedArticles(): Flow<List<Artigo>> =
        dao.observePublished().map { articles -> articles.map(ArticleEntity::toDomain) }

    override suspend fun getArticle(articleId: String): Artigo? = dao.getById(articleId)?.toDomain()

    override suspend fun seedIfEmpty() {
        if (dao.count() == 0) dao.upsertAll(ArticleSeedData.create())
    }
}

class LocalChatRepository(private val database: AppDatabase) : ChatRepository {
    private val dao = database.chatDao()

    override fun observeMessages(userId: String): Flow<List<MensagemChat>> =
        dao.observeForUser(userId).map { messages -> messages.map(ChatMessageEntity::toDomain) }

    override suspend fun ensureGreeting(userId: String, sessionId: String) {
        if (dao.countForUser(userId) == 0) {
            dao.insert(
                ChatMessageEntity(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    sessionId = sessionId,
                    text = "Olá! Sou uma assistente informativa do app. Posso explicar conteúdos gerais de saúde feminina, mas não substituo uma consulta ou serviço de emergência.",
                    isUser = false,
                    sentAt = System.currentTimeMillis()
                )
            )
        }
    }

    override suspend fun saveMessage(message: MensagemChat) {
        dao.insert(message.toEntity())
    }

    override suspend fun clearHistory(userId: String) {
        dao.clearForUser(userId)
    }
}

private fun UserEntity.toDomain() = Usuario(id, displayName, email, createdAt, updatedAt)
private fun ProfileEntity.toDomain() = PerfilUsuario(
    usuarioId = userId,
    faseVida = runCatching { FaseVida.valueOf(lifeStage) }.getOrDefault(FaseVida.IDADE_REPRODUTIVA),
    estaGestante = isPregnant,
    dataPapanicolau = papSmearDate,
    dataMamografia = mammogramDate,
    fotoUri = photoUri
)
private fun PerfilUsuario.toEntity() = ProfileEntity(
    userId = usuarioId,
    lifeStage = faseVida.name,
    isPregnant = estaGestante,
    papSmearDate = dataPapanicolau,
    mammogramDate = dataMamografia,
    photoUri = fotoUri
)
private fun SymptomRecordEntity.toDomain() = RegistroSintoma(
    id = id,
    usuarioId = userId,
    dataTimestamp = dataTimestamp,
    tipo = runCatching { SintomaTipo.valueOf(type) }.getOrDefault(SintomaTipo.OUTRO),
    intensidade = intensity,
    notas = notes,
    criadoEm = createdAt,
    atualizadoEm = updatedAt
)
private fun RegistroSintoma.toEntity() = SymptomRecordEntity(
    id = id,
    userId = usuarioId,
    dataTimestamp = dataTimestamp,
    type = tipo.name,
    intensity = intensidade,
    notes = notas?.trim()?.takeIf(String::isNotEmpty),
    createdAt = criadoEm,
    updatedAt = atualizadoEm
)
private fun ArticleEntity.toDomain() = Artigo(
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
private fun ChatMessageEntity.toDomain() = MensagemChat(id, userId, sessionId, text, isUser, sentAt)
private fun MensagemChat.toEntity() = ChatMessageEntity(id, usuarioId, sessaoId, texto, isUsuario, enviadoEm)
