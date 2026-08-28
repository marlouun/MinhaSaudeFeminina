package com.example.minhasaudefeminina.app

import android.content.Context
import com.example.minhasaudefeminina.data.local.AppDatabase
import com.example.minhasaudefeminina.data.local.SessionPreferences
import com.example.minhasaudefeminina.data.repository.ArticleRepository
import com.example.minhasaudefeminina.data.repository.AuthRepository
import com.example.minhasaudefeminina.data.repository.ChatRepository
import com.example.minhasaudefeminina.data.repository.LocalAuthRepository
import com.example.minhasaudefeminina.data.repository.LocalChatRepository
import com.example.minhasaudefeminina.data.repository.LocalProfileRepository
import com.example.minhasaudefeminina.data.repository.LocalSymptomRepository
import com.example.minhasaudefeminina.data.repository.ProfileRepository
import com.example.minhasaudefeminina.data.repository.SupabaseArticleRepository
import com.example.minhasaudefeminina.data.repository.SymptomRepository

class AppContainer(context: Context) {
    private val database = AppDatabase.create(context)
    private val sessionPreferences = SessionPreferences(context.applicationContext)

    val authRepository: AuthRepository = LocalAuthRepository(database, sessionPreferences)
    val profileRepository: ProfileRepository = LocalProfileRepository(database)
    val symptomRepository: SymptomRepository = LocalSymptomRepository(database)
    val articleRepository: ArticleRepository = SupabaseArticleRepository(database)
    val chatRepository: ChatRepository = LocalChatRepository(database)
}
