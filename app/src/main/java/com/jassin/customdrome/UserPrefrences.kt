package com.jassin.customdrome

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class UserPreferences(private val context: Context) {

    companion object {
        val SERVER_URL: Preferences.Key<String> = stringPreferencesKey("server_url")
        val USER_NAME: Preferences.Key<String> = stringPreferencesKey("user_name")
    }

    // Read values (as a Flow)
    val userName: Flow<String?> = context.dataStore.data.map { it[USER_NAME] }
    val serverURL: Flow<String?> = context.dataStore.data.map { it[SERVER_URL] }

    // Save values
    suspend fun saveUsername(name: String) {
        context.dataStore.edit { it[USER_NAME] = name }
    }
    suspend fun saveServerURL(url: String) {
        context.dataStore.edit { it[SERVER_URL] = url }
    }
}
