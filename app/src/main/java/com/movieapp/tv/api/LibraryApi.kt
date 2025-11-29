package com.movieapp.tv.api

import com.movieapp.tv.model.LibraryItem
import retrofit2.http.GET

interface LibraryApi {
    @GET("files")
    suspend fun getLibraryFiles(): List<LibraryItem>
    
    @GET("series")
    suspend fun getLibrarySeries(): List<LibraryItem>
    
    @GET("folder")
    suspend fun getFolderContents(): List<LibraryItem>
}
