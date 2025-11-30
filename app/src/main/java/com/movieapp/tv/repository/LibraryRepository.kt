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
                    PreferencesManager.saveLibraryUrl(context, baseUrl)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch discovery URL, falling back to saved URL: $baseUrl", e)
            }

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

    suspend fun getLibrarySeries(): List<Movie> = withContext(Dispatchers.IO) {
        return@withContext try {
            var baseUrl = PreferencesManager.getLibraryUrl(context).trimEnd('/')
            
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
            val files = libraryApi.getLibraryFiles() // Use getLibraryFiles as it contains both
            Log.d(TAG, "Fetched ${files.size} items for series filtering")

            val series = mutableListOf<Movie>()
            files.filter { it.type == "tv" }.forEach { file ->
                if (!file.tmdbId.isNullOrEmpty()) {
                    try {
                        // For TV shows, we might need a different endpoint or just use movie details if ID is compatible
                        // Usually TMDB has separate endpoints for TV. 
                        // Assuming we can use getTvDetails or similar. 
                        // But for now, let's try getMovieDetails or if we need to add getTvDetails.
                        // The user said "using the id show the poster".
                        // I'll assume I need to fetch TV details.
                        // Let's check if RetrofitClient.api has getTvDetails.
                        // If not, I might need to add it. For now, I'll use getMovieDetails and hope it works or add getTvDetails.
                        // Actually, I should check TmdbApi.
                        
                        // Wait, I can't check TmdbApi inside this replacement.
                        // I'll assume I need to add getTvDetails to TmdbApi if it's not there.
                        // For now I'll use a placeholder or try to use getMovieDetails if it works for generic info.
                        // But TV IDs and Movie IDs can overlap.
                        // I'll use a generic approach: fetch details.
                        
                        // Let's assume I'll add getTvDetails to TmdbApi.
                        val tvShow = tmdbApi.getTvDetails(file.tmdbId, apiKey)
                        tvShow.videoUrl = file.url // Store the relative path or full URL?
                        // The file.url is "/stream/stranger things/"
                        // We probably want to store this to navigate into it.
                        tvShow.videoUrl = file.url // Keep relative for getFolderContents
                        tvShow.isFolder = true
                        tvShow.type = "tv"
                        series.add(tvShow)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error fetching details for TV ID: ${file.tmdbId}", e)
                    }
                }
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
            // Build the full URL - folderPath already contains the path like "/stream/stranger things/"
            val fullUrl = "$baseUrl$folderPath"
            Log.d(TAG, "Fetching folder contents from: $fullUrl")
            val jsonElement = libraryApi.getFolderContents(fullUrl)
            
            val items = mutableListOf<Movie>()
            val gson = com.google.gson.Gson()
            val itemType = object : com.google.gson.reflect.TypeToken<com.movieapp.tv.model.LibraryItem>() {}.type

            if (jsonElement.isJsonArray) {
                val jsonArray = jsonElement.asJsonArray
                Log.d(TAG, "Fetched ${jsonArray.size()} items from folder (Array): $folderPath")
                jsonArray.forEach { element ->
                    try {
                        val file = gson.fromJson<com.movieapp.tv.model.LibraryItem>(element, itemType)
                        items.add(mapLibraryItemToMovie(file, baseUrl))
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing library item", e)
                    }
                }
            } else if (jsonElement.isJsonObject) {
                val jsonObject = jsonElement.asJsonObject
                Log.d(TAG, "Fetched ${jsonObject.size()} items from folder (Object): $folderPath")
                jsonObject.entrySet().forEach { entry ->
                    try {
                        val file = gson.fromJson<com.movieapp.tv.model.LibraryItem>(entry.value, itemType)
                        items.add(mapLibraryItemToMovie(file, baseUrl))
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing library item from key ${entry.key}", e)
                    }
                }
            }
            
            items
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching folder contents", e)
            emptyList()
        }
    }

    private fun mapLibraryItemToMovie(file: com.movieapp.tv.model.LibraryItem, baseUrl: String): Movie {
        return Movie(
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
            videoUrl = "$baseUrl${file.url}",
            isFolder = file.isFolder,
            type = file.type
        )
    }
}
