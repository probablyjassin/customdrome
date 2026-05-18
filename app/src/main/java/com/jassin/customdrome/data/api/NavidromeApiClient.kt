@file:Suppress("unused")

package com.jassin.customdrome.data.api

import android.util.Log
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.URLBuilder
import io.ktor.http.encodedPath
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

    private companion object {
        const val TAG = "NavidromeApiClient"
    }

    private fun buildAuthenticatedUrl(
        serverUrl: String,
        pathSegments: List<String>,
        queryParameters: Map<String, String>,
    ): String =
        URLBuilder(serverUrl.trimEnd('/'))
            .apply {
                pathSegments.forEach { encodedPath += "/$it" }
                queryParameters.forEach { (key, value) -> parameters.append(key, value) }
            }.buildString()

    suspend fun pingAuth(
        serverUrl: String,
        token: String,
    ): Boolean {
        val baseUrl = serverUrl.trimEnd('/')
        Log.d(TAG, "pingAuth -> $baseUrl/api/song")
        val response =
            client.get("$baseUrl/api/song") {
                header("x-nd-authorization", "Bearer $token")
            }
        Log.d(TAG, "pingAuth response status=${response.status}")
        return response.status.isSuccess()
    }

    suspend fun fetchSongs(
        serverUrl: String,
        token: String,
    ): List<SongDto> {
        val baseUrl = serverUrl.trimEnd('/')
        Log.d(TAG, "fetchSongs -> $baseUrl/api/song")
        val response =
            client.get("$baseUrl/api/song") {
                header("x-nd-authorization", "Bearer $token")
            }

        if (!response.status.isSuccess()) {
            Log.d(TAG, "fetchSongs failed: ${response.status}")
            return emptyList()
        }

        return try {
            val songs = response.body<List<SongDto>>()
            Log.d(TAG, "fetchSongs success: count=${songs.size}")
            songs
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing songs response", e)
            emptyList()
        }
    }

    suspend fun fetchSongCount(
        serverUrl: String,
        token: String,
    ): Int = fetchSongs(serverUrl, token).size

    suspend fun fetchCoverArt(
        serverUrl: String,
        username: String,
        subsonicToken: String,
        subsonicSalt: String,
        songId: String,
        apiVersion: String = "1.16.1",
        clientName: String = "CustomDrome",
    ): ByteArray? {
        Log.d(TAG, "fetchCoverArt requested for songId=$songId")
        val url =
            buildAuthenticatedUrl(
                serverUrl = serverUrl,
                pathSegments = listOf("rest", "getCoverArt"),
                queryParameters =
                    mapOf(
                        "u" to username,
                        "t" to subsonicToken,
                        "s" to subsonicSalt,
                        "v" to apiVersion,
                        "c" to clientName,
                        "id" to songId,
                    ),
            )
        val response =
            client.get(url) {
                header("Accept", "image/*")
            }

        if (!response.status.isSuccess()) {
            Log.d(TAG, "fetchCoverArt failed for songId=$songId: ${response.status}")
            return null
        }

        return try {
            val bytes: ByteArray = response.body()
            Log.d(TAG, "fetchCoverArt success for songId=$songId: bytes=${bytes.size}")
            bytes
        } catch (e: Exception) {
            Log.e(TAG, "Error reading cover art bytes for songId=$songId", e)
            null
        }
    }

    suspend fun resolveStreamUrl(
        serverUrl: String,
        username: String,
        subsonicToken: String,
        subsonicSalt: String,
        songId: String,
        apiVersion: String = "1.16.1",
        clientName: String = "CustomDrome",
    ): String =
        buildAuthenticatedUrl(
            serverUrl = serverUrl,
            pathSegments = listOf("rest", "stream"),
            queryParameters =
                mapOf(
                    "u" to username,
                    "t" to subsonicToken,
                    "s" to subsonicSalt,
                    "v" to apiVersion,
                    "c" to clientName,
                    "id" to songId,
                ),
        ).also {
            Log.d(TAG, "resolveStreamUrl built stream URL for songId=$songId")
        }
}
