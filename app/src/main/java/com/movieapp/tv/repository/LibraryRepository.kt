package com.movieapp.tv.repository

import android.content.Context
import android.util.Log
import com.movieapp.tv.api.RetrofitClient
import com.movieapp.tv.model.Movie
import com.movieapp.tv.utils.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LibraryRepository(private val context: Context) {
    private val tmdbApi = RetrofitClient.api
    private val apiKey = "879b99242974f2ee8447dd76534e0fd8" // Ideally this should be injected or secured

    companion object {
        private const val TAG = "LibraryRepository"
        private var cachedMovies: List<Movie> = emptyList()
    }

    fun getCachedMovies(): List<Movie> {
        return cachedMovies
    }

    suspend fun getLibraryMovies(): List<Movie> = withContext(Dispatchers.IO) {
        return@withContext try {
            var baseUrl = PreferencesManager.getLibraryUrl(context).trimEnd('/')
            
            // Try to get dynamic URL from Discovery service
            try {
                val discoveryResponse = RetrofitClient.discoveryApi.getAppUrl()
                if (discoveryResponse.status == "ready" && discoveryResponse.url.isNotEmpty()) {
                    baseUrl = discoveryResponse.url.trimEnd('/')
                    Log.d(TAG, "Discovered new library URL: $baseUrl")
                    
                    // Update preferences with the new URL so it's visible in settings
                    // and available for next time/offline fallback
                    PreferencesManager.saveLibraryUrl(context, baseUrl)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch discovery URL, falling back to saved URL: $baseUrl", e)
            }

            // Create API client with the determined base URL
            val libraryApi = RetrofitClient.createLibraryApi(baseUrl)
            
            val files = libraryApi.getLibraryFiles()
            Log.d(TAG, "Fetched ${files.size} files from library")

            val movies = mutableListOf<Movie>()
            
            files.forEach { file ->
                if (file.tmdbId.isNotEmpty()) {
                    try {
                        val movie = tmdbApi.getMovieDetails(file.tmdbId, apiKey)
                        // Construct the full video URL using dynamic base URL
                        movie.videoUrl = "$baseUrl${file.url}"
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

    suspend fun getLibrarySeries(): List<Movie> = withContext(Dispatchers.IO) {
        return@withContext try {
            var baseUrl = PreferencesManager.getLibraryUrl(context).trimEnd('/')
            
            // Try to get dynamic URL from Discovery service
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

            val libraryApi = RetrofitClient.createLibraryApi(baseUrl)
            val files = libraryApi.getLibrarySeries()
            Log.d(TAG, "Fetched ${files.size} series from library")

            val series = mutableListOf<Movie>()
            files.forEach { file ->
                val movie = Movie(
                    id = 0,
                    _title = file.name,
                    _name = null,
                    overview = "",
                    posterPath = null,
                    backdropPath = null,
                    voteAverage = 0.0,
                    _releaseDate = null,
                    _firstAirDate = null,
                    originalLanguage = null,
                    videoUrl = "$baseUrl${file.url}"
                )
                series.add(movie)
            }
            series
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching library series", e)
            emptyList()
        }
    }

    suspend fun getFolderContents(folderPath: String): List<Movie> = withContext(Dispatchers.IO) {
        return@withContext try {
            var baseUrl = PreferencesManager.getLibraryUrl(context).trimEnd('/')
            
            // Try to get dynamic URL from Discovery service
            try {
                val discoveryResponse = RetrofitClient.discoveryApi.getAppUrl()
                if (discoveryResponse.status == "ready" && discoveryResponse.url.isNotEmpty()) {
                    baseUrl = discoveryResponse.url.trimEnd('/')
                    PreferencesManager.saveLibraryUrl(context, baseUrl)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch discovery URL, falling back to saved URL: $baseUrl", e)
            }

            val libraryApi = RetrofitClient.createLibraryApi(baseUrl)
            val files = libraryApi.getFolderContents()
            Log.d(TAG, "Fetched ${files.size} items from folder")

            val items = mutableListOf<Movie>()
            files.forEach { file ->
                val movie = Movie(
                    id = 0,
                    _title = file.name,
                    _name = null,
                    overview = "",
                    posterPath = null,
                    backdropPath = null,
                    voteAverage = 0.0,
                    _releaseDate = null,
                    _firstAirDate = null,
                    originalLanguage = null,
                    videoUrl = "$baseUrl${file.url}"
                )
                items.add(movie)
            }
            items
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching folder contents", e)
            emptyList()
        }
    }
}
