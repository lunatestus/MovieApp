package com.movieapp.tv.model

import com.google.gson.annotations.SerializedName

data class LibraryItem(
    @SerializedName("name")
    val name: String,
    
    @SerializedName("size")
    val size: Long,
    
    @SerializedName("tmdb_id")
    val tmdbId: String,
    
    @SerializedName("url")
    val url: String
)
