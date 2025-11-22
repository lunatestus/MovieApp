package com.movieapp.tv.viewmodel

import androidx.lifecycle.ViewModel
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

class LibraryViewModel : ViewModel() {

    private val repository = LibraryRepository()
    
    private val _uiState = MutableStateFlow<LibraryUiState>(LibraryUiState.Loading)
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        loadLibrary()
    }

    fun loadLibrary() {
        viewModelScope.launch {
            _uiState.value = LibraryUiState.Loading
            val movies = repository.getLibraryMovies()
            if (movies.isEmpty()) {
                _uiState.value = LibraryUiState.Error("No videos found in library.")
            } else {
                _uiState.value = LibraryUiState.Success(movies)
            }
        }
    }
}
