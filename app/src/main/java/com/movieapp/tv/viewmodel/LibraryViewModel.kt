package com.movieapp.tv.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.movieapp.tv.model.Movie
import com.movieapp.tv.repository.LibraryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class LibraryUiState {
    object Loading : LibraryUiState()
    data class Success(val movies: List<Movie>) : LibraryUiState()
    data class Error(val message: String) : LibraryUiState()
}

class LibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = LibraryRepository(application)
    
    private val _uiState = MutableStateFlow<LibraryUiState>(LibraryUiState.Loading)
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        loadLibrary()
    }

    fun loadLibrary() {
        viewModelScope.launch {
            // Emit cached data immediately if available
            val cached = repository.getCachedMovies()
            if (cached.isNotEmpty()) {
                _uiState.value = LibraryUiState.Success(cached)
            } else {
                _uiState.value = LibraryUiState.Loading
            }
            
            // Fetch fresh data
            val movies = repository.getLibraryMovies()
            if (movies.isEmpty()) {
                if (cached.isEmpty()) {
                    _uiState.value = LibraryUiState.Error("No videos found in library.")
                }
                // If we have cache but fetch failed/returned empty, we might want to keep showing cache 
                // or show error. For now, if fetch returns empty, let's assume it's empty.
                // However, if it was a network error (which returns emptyList in repo catch block),
                // we might want to distinguish. 
                // Given the repo returns emptyList on error, we can't distinguish easily without changing repo.
                // But showing "No videos" is better than crashing. 
                // If we have cache, maybe we should stick with it? 
                // Let's only show Error if we don't have cache.
            } else {
                _uiState.value = LibraryUiState.Success(movies)
            }
        }
    }

}
