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

sealed class SeriesUiState {
    object Loading : SeriesUiState()
    data class Success(val series: List<Movie>) : SeriesUiState()
    data class Error(val message: String) : SeriesUiState()
}

class SeriesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = LibraryRepository(application)
    
    private val _uiState = MutableStateFlow<SeriesUiState>(SeriesUiState.Loading)
    val uiState: StateFlow<SeriesUiState> = _uiState.asStateFlow()

    init {
        loadSeries()
    }

    fun loadSeries() {
        viewModelScope.launch {
            _uiState.value = SeriesUiState.Loading
            
            val series = repository.getLibrarySeries()
            if (series.isEmpty()) {
                _uiState.value = SeriesUiState.Error("No series found in library.")
            } else {
                _uiState.value = SeriesUiState.Success(series)
            }
        }
    }
}
