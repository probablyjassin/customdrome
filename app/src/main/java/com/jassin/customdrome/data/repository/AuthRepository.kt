package com.jassin.customdrome.data.repository

import android.util.Log
import com.jassin.customdrome.UserPreferences
import com.jassin.customdrome.data.api.NavidromeApiClient
import com.jassin.customdrome.data.local.SongCacheDatabase
import com.jassin.customdrome.data.models.HomeLoadResult
import kotlinx.coroutines.flow.first

class AuthRepository(
    private val userPrefs: UserPreferences,
    private val apiClient: NavidromeApiClient,
    private val songCacheDatabase: SongCacheDatabase,
) {
    suspend fun checkLoginAndGetSongCount(): HomeLoadResult {
        val token = userPrefs.token.first()
        val serverUrl = userPrefs.serverURL.first()

        if (token.isNullOrBlank() || serverUrl.isNullOrBlank()) {
            return HomeLoadResult.NotLoggedIn
        }

        val loggedIn = apiClient.pingAuth(serverUrl, token)
        if (!loggedIn) {
            return HomeLoadResult.NotLoggedIn
        }

        if (songCacheDatabase.isSongsCacheInitialized()) {
            Log.d("AuthRepository", "Using cached songs: ${songCacheDatabase.getSongCount()} songs")
            return HomeLoadResult.LoggedIn(songCount = songCacheDatabase.getSongCount())
        }

        Log.d("AuthRepository", "Making http request to fetch songs")
        val songs = apiClient.fetchSongs(serverUrl, token)
        songCacheDatabase.replaceAllSongs(songs)
        return HomeLoadResult.LoggedIn(songCount = songs.size)
    }
}
