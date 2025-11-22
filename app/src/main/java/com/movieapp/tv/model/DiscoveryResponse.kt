package com.movieapp.tv.model

import com.google.gson.annotations.SerializedName

data class DiscoveryResponse(
    @SerializedName("status")
    val status: String,
    
    @SerializedName("url")
    val url: String
)
