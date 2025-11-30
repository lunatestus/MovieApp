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

sealed class SeriesDetailsUiState {
    object Loading : SeriesDetailsUiState()
    data class Success(val episodes: List<Movie>) : SeriesDetailsUiState()
    data class Error(val message: String) : SeriesDetailsUiState()
}

class SeriesDetailsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = LibraryRepository(application)
    
    private val _uiState = MutableStateFlow<SeriesDetailsUiState>(SeriesDetailsUiState.Loading)
    val uiState: StateFlow<SeriesDetailsUiState> = _uiState.asStateFlow()

    fun loadEpisodes(path: String) {
        viewModelScope.launch {
            _uiState.value = SeriesDetailsUiState.Loading
            
            val episodes = repository.getFolderContents(path)
            if (episodes.isEmpty()) {
                _uiState.value = SeriesDetailsUiState.Error("No episodes found.")
            } else {
                _uiState.value = SeriesDetailsUiState.Success(episodes)
            }
        }
    }
}
