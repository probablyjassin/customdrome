package com.jassin.customdrome.data.api

import android.util.Log
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable

@Serializable
data class SongDto(
    val id: String? = null,
    val title: String? = null,
    val album: String? = null,
    val artist: String? = null,
    val year: Int? = null,
    val genre: String? = null,
)

class NavidromeApiClient {
    private val client = HttpClientProvider.client

    suspend fun pingAuth(
        serverUrl: String,
        token: String,
    ): Boolean {
        val baseUrl = serverUrl.trimEnd('/')
        val response =
            client.get("$baseUrl/api/song") {
                header("x-nd-authorization", "Bearer $token")
            }
        Log.d("NavidromeApiClient", "pingAuth response: ${response.bodyAsText()}")
        return response.status.isSuccess()
    }

    suspend fun fetchSongs(
        serverUrl: String,
        token: String,
    ): List<SongDto> {
        val baseUrl = serverUrl.trimEnd('/')
        val response =
            client.get("$baseUrl/api/song") {
                header("x-nd-authorization", "Bearer $token")
            }

        if (!response.status.isSuccess()) {
            Log.d("NavidromeApiClient", "fetchSongs failed: ${response.status}")
            return emptyList()
        }

        return try {
            response.body<List<SongDto>>()
        } catch (e: Exception) {
            Log.e("NavidromeApiClient", "Error parsing songs response", e)
            emptyList()
        }
    }

    suspend fun fetchSongCount(
        serverUrl: String,
        token: String,
    ): Int = fetchSongs(serverUrl, token).size
}
