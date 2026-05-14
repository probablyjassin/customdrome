package com.jassin.customdrome.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jassin.customdrome.UserPreferences
import com.jassin.customdrome.data.api.NavidromeApiClient
import com.jassin.customdrome.data.local.CoverArtCache
import com.jassin.customdrome.data.local.SongCacheDatabase
import com.jassin.customdrome.data.models.SongsUiState
import com.jassin.customdrome.data.models.SongsViewModel
import com.jassin.customdrome.data.repository.SongsRepository
import com.jassin.customdrome.ui.common.SingleSongDisplay

@androidx.compose.runtime.Composable
fun Songs(userPrefs: UserPreferences) {
    val context = LocalContext.current
    val apiClient = remember { NavidromeApiClient() }
    val songCacheDatabase = remember { SongCacheDatabase(context.applicationContext) }
    val coverArtCache = remember { CoverArtCache(context.applicationContext) }
    val songsRepository = remember(userPrefs, songCacheDatabase, coverArtCache) {
        SongsRepository(userPrefs, apiClient, songCacheDatabase, coverArtCache)
    }

    val vm: SongsViewModel =
        viewModel(
            factory =
                object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T =
                        SongsViewModel(songsRepository) as T
                },
        )

    val songsState by vm.songsState.collectAsState()
    val coverCache = remember { mutableStateOf<Map<String, ByteArray>>(emptyMap()) }

    when (val state = songsState) {
        SongsUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize().safeDrawingPadding(),
                contentAlignment = Alignment.Center,
            ) {
                androidx.compose.material3.Text("Loading songs...")
            }
        }

        is SongsUiState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize().safeDrawingPadding(),
                contentAlignment = Alignment.Center,
            ) {
                androidx.compose.material3.Text("Error: ${state.message}")
            }
        }

        is SongsUiState.Ready -> {
            if (state.songs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().safeDrawingPadding(),
                    contentAlignment = Alignment.Center,
                ) {
                    androidx.compose.material3.Text("No songs found")
                }
            } else {
                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier.fillMaxSize().safeDrawingPadding(),
                ) {
                    items(state.songs, key = { it.id }) { song ->
                        SingleSongDisplay(
                            title = song.title,
                            artist = song.artist,
                            songId = song.id,
                            songsRepository = songsRepository,
                            onCoverLoaded = { songId, coverBytes ->
                                val current = coverCache.value
                                if (!current.containsKey(songId)) {
                                    coverCache.value = current + (songId to coverBytes)
                                }
                            },
                            cachedCover = coverCache.value[song.id],
                        )
                    }
                }
            }
        }
    }
}
