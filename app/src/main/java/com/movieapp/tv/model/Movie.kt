package com.movieapp.tv.model

import com.google.gson.annotations.SerializedName

data class Movie(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("overview")
    val overview: String,
    
    @SerializedName("poster_path")
    val posterPath: String?,
    
    @SerializedName("backdrop_path")
    val backdropPath: String?,
    
    @SerializedName("vote_average")
    val voteAverage: Double,
    
    @SerializedName("release_date")
    val releaseDate: String,
    
    @SerializedName("original_language")
    val originalLanguage: String?,

    @SerializedName("genre_ids")
    val genreIds: List<Int>,

    // Not part of TMDB API, used for Library items
    var videoUrl: String? = null
) {
    fun getPosterUrl(): String {
        return "https://image.tmdb.org/t/p/w500$posterPath"
    }
    
    fun getBackdropUrl(): String {
        return "https://image.tmdb.org/t/p/original$backdropPath"
    }

    fun getFormattedYear(): String {
        return if (releaseDate.length >= 4) releaseDate.take(4) else ""
    }

    fun getFormattedRating(): String {
        return String.format("%.1f", voteAverage)
    }

    fun getFormattedLanguage(): String {
        return originalLanguage?.uppercase() ?: "EN"
    }
}

data class MovieResponse(
    @SerializedName("page")
    val page: Int,
    
    @SerializedName("results")
    val results: List<Movie>,
    
    @SerializedName("total_pages")
    val totalPages: Int,
    
    @SerializedName("total_results")
    val totalResults: Int
)
