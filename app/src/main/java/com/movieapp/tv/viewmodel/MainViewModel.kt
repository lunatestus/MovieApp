package com.movieapp.tv.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movieapp.tv.model.Movie
import com.movieapp.tv.repository.MovieRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class MainUiState {
    object Loading : MainUiState()
    data class Success(
        val nowPlaying: List<Movie>,
        val popular: List<Movie>,
        val topRated: List<Movie>,
        val upcoming: List<Movie>
    ) : MainUiState()
    data class Error(val message: String) : MainUiState()
}

class MainViewModel : ViewModel() {

    private val repository = MovieRepository()
    
    private val _uiState = MutableStateFlow<MainUiState>(MainUiState.Loading)
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        loadMovies()
    }

    fun loadMovies() {
        viewModelScope.launch {
            _uiState.value = MainUiState.Loading
            
            try {
                // Load all categories in parallel
                val nowPlayingDeferred = async { repository.getNowPlayingMovies() }
                val popularDeferred = async { repository.getPopularMovies() }
                val topRatedDeferred = async { repository.getTopRatedMovies() }
                val upcomingDeferred = async { repository.getUpcomingMovies() }

                val nowPlaying = nowPlayingDeferred.await()
                val popular = popularDeferred.await()
                val topRated = topRatedDeferred.await()
                val upcoming = upcomingDeferred.await()

                if (nowPlaying.isEmpty() && popular.isEmpty() && topRated.isEmpty() && upcoming.isEmpty()) {
                    _uiState.value = MainUiState.Error("No movies found. Please check your internet connection.")
                } else {
                    _uiState.value = MainUiState.Success(
                        nowPlaying = nowPlaying,
                        popular = popular,
                        topRated = topRated,
                        upcoming = upcoming
                    )
                }
            } catch (e: Exception) {
                _uiState.value = MainUiState.Error("Failed to load movies: ${e.message}")
            }
        }
    }
}
