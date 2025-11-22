package com.movieapp.tv.repository

import android.util.Log
import com.movieapp.tv.api.RetrofitClient
import com.movieapp.tv.model.Movie
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MovieRepository {
    private val api = RetrofitClient.api
    private val apiKey = "879b99242974f2ee8447dd76534e0fd8"

    companion object {
        private const val TAG = "MovieRepository"
    }

    suspend fun getPopularMovies(): List<Movie> = withContext(Dispatchers.IO) {
        return@withContext try {
            val response = api.getPopularMovies(apiKey)
            Log.d(TAG, "Fetched ${response.results.size} popular movies")
            response.results.filter { it.posterPath != null }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching popular movies", e)
            emptyList()
        }
    }

    suspend fun getTopRatedMovies(): List<Movie> = withContext(Dispatchers.IO) {
        return@withContext try {
            val response = api.getTopRatedMovies(apiKey)
            Log.d(TAG, "Fetched ${response.results.size} top rated movies")
            response.results.filter { it.posterPath != null }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching top rated movies", e)
            emptyList()
        }
    }

    suspend fun getUpcomingMovies(): List<Movie> = withContext(Dispatchers.IO) {
        return@withContext try {
            val response = api.getUpcomingMovies(apiKey)
            Log.d(TAG, "Fetched ${response.results.size} upcoming movies")
            response.results.filter { it.posterPath != null }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching upcoming movies", e)
            emptyList()
        }
    }

    suspend fun getNowPlayingMovies(): List<Movie> = withContext(Dispatchers.IO) {
        return@withContext try {
            val response = api.getNowPlayingMovies(apiKey)
            Log.d(TAG, "Fetched ${response.results.size} now playing movies")
            response.results.filter { it.posterPath != null }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching now playing movies", e)
            emptyList()
        }
    }
}
