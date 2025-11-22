package com.movieapp.tv.api

import com.movieapp.tv.model.DiscoveryResponse
import retrofit2.http.GET

interface DiscoveryApi {
    @GET("/")
    suspend fun getAppUrl(): DiscoveryResponse
}
