package com.jassin.customdrome

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class UserPreferences(
    private val context: Context,
) {
    companion object {
        val SERVER_URL: Preferences.Key<String> = stringPreferencesKey("server_url")
        val USER_NAME: Preferences.Key<String> = stringPreferencesKey("user_name")
        val PASSWORD: Preferences.Key<String> = stringPreferencesKey("password")
        val TOKEN: Preferences.Key<String> = stringPreferencesKey("token")
    }

    // Read values (as a Flow)
    val userName: Flow<String?> = context.dataStore.data.map { it[USER_NAME] }
    val serverURL: Flow<String?> = context.dataStore.data.map { it[SERVER_URL] }
    val password: Flow<String?> = context.dataStore.data.map { it[PASSWORD] }
    val token: Flow<String?> = context.dataStore.data.map { it[TOKEN] }

    // Save values
    suspend fun saveUsername(name: String) {
        context.dataStore.edit { it[USER_NAME] = name }
    }

    suspend fun saveServerURL(url: String) {
        context.dataStore.edit { it[SERVER_URL] = url }
    }

    suspend fun savePassword(password: String) {
        context.dataStore.edit { it[PASSWORD] = password }
    }

    suspend fun saveToken(token: String) {
        context.dataStore.edit { it[TOKEN] = token }
    }
}
