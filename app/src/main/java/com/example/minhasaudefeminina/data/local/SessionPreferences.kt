package com.example.minhasaudefeminina.data.local

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.sessionDataStore by preferencesDataStore(name = "session_preferences")

class SessionPreferences(private val context: Context) {
    private object Keys {
        val currentUserId = stringPreferencesKey("current_user_id")
    }

    val currentUserId: Flow<String?> = context.sessionDataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { preferences: Preferences -> preferences[Keys.currentUserId] }

    suspend fun setCurrentUser(userId: String) {
        context.sessionDataStore.edit { it[Keys.currentUserId] = userId }
    }

    suspend fun clearCurrentUser() {
        context.sessionDataStore.edit { it.remove(Keys.currentUserId) }
    }
}
