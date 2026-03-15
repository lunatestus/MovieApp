package com.movieapp.tv.repository

import android.content.Context
import android.util.Log
import com.movieapp.tv.BuildConfig
import com.movieapp.tv.api.RetrofitClient
import com.movieapp.tv.model.Movie
import com.movieapp.tv.utils.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LibraryRepository(private val context: Context) {
    private val tmdbApi = RetrofitClient.api
    private val apiKey = BuildConfig.TMDB_API_KEY

    companion object {
        private const val TAG = "LibraryRepository"
        private var cachedMovies: List<Movie> = emptyList()
    }

    fun getCachedMovies(): List<Movie> {
        return cachedMovies
    }

    private fun hasApiKey(): Boolean {
        if (apiKey.isBlank()) {
            Log.e(TAG, "TMDB API key is missing. Set tmdb.apiKey in local.properties.")
            return false
        }
        return true
    }

    private suspend fun resolveBaseUrl(): String {
        var baseUrl = PreferencesManager.getLibraryUrl(context).trimEnd('/')

        try {
            val discoveryResponse = RetrofitClient.discoveryApi.getAppUrl()
            if (discoveryResponse.status == "ready" && discoveryResponse.url.isNotEmpty()) {
                baseUrl = discoveryResponse.url.trimEnd('/')
                Log.d(TAG, "Discovered new library URL: $baseUrl")
                PreferencesManager.saveLibraryUrl(context, baseUrl)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch discovery URL, falling back to saved URL: $baseUrl", e)
        }

        return baseUrl
    }

    suspend fun getLibraryMovies(): List<Movie> = withContext(Dispatchers.IO) {
        if (!hasApiKey()) {
            return@withContext emptyList()
        }
        return@withContext try {
            val baseUrl = resolveBaseUrl()
            val libraryApi = RetrofitClient.createLibraryApi(baseUrl)
            val files = libraryApi.getLibraryFiles()
            Log.d(TAG, "Fetched ${files.size} files from library")

            val movies = mutableListOf<Movie>()
            
            files.filter { it.type == "movie" }.forEach { file ->
                if (!file.tmdbId.isNullOrEmpty()) {
                    try {
                        val movie = tmdbApi.getMovieDetails(file.tmdbId, apiKey)
                        movie.videoUrl = "$baseUrl${file.url}"
                        movie.type = "movie"
                        movie.isFolder = false
                        movies.add(movie)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error fetching details for TMDB ID: ${file.tmdbId}", e)
                    }
                }
            }
            movies.also { cachedMovies = it }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching library files", e)
            emptyList()
        }
    }
}
