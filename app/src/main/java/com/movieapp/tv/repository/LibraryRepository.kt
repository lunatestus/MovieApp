package com.movieapp.tv.repository

import android.util.Log
import com.movieapp.tv.api.RetrofitClient
import com.movieapp.tv.model.Movie
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LibraryRepository {
    private val libraryApi = RetrofitClient.libraryApi
    private val tmdbApi = RetrofitClient.api
    private val apiKey = "879b99242974f2ee8447dd76534e0fd8" // Ideally this should be injected or secured

    companion object {
        private const val TAG = "LibraryRepository"
        private const val LIBRARY_BASE_URL = "https://yashkushwahayt--modal-video-uploader-flask-app.modal.run"
    }

    suspend fun getLibraryMovies(): List<Movie> = withContext(Dispatchers.IO) {
        return@withContext try {
            val files = libraryApi.getLibraryFiles()
            Log.d(TAG, "Fetched ${files.size} files from library")

            val movies = mutableListOf<Movie>()
            
            files.forEach { file ->
                if (file.tmdbId.isNotEmpty()) {
                    try {
                        val movie = tmdbApi.getMovieDetails(file.tmdbId, apiKey)
                        // Construct the full video URL
                        // The API returns relative path starting with /stream/..., so just append to base
                        movie.videoUrl = "$LIBRARY_BASE_URL${file.url}"
                        movies.add(movie)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error fetching details for TMDB ID: ${file.tmdbId}", e)
                    }
                }
            }
            movies
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching library files", e)
            emptyList()
        }
    }
}
