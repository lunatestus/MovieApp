package com.movieapp.tv.api

import android.content.Context
import com.movieapp.tv.utils.PreferencesManager
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "https://api.themoviedb.org/3/"
    private const val DISCOVERY_URL = "https://yashkushwahayt--modal-video-uploader-get-app-url-dev.modal.run/"
    private const val CONNECT_TIMEOUT = 30L
    private const val READ_TIMEOUT = 30L
    private const val WRITE_TIMEOUT = 30L
    
    private var currentLibraryUrl: String? = null
    private var libraryRetrofit: Retrofit? = null
    private var _libraryApi: LibraryApi? = null
    
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }
    
    val api: TmdbApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TmdbApi::class.java)
    }

    val discoveryApi: DiscoveryApi by lazy {
        Retrofit.Builder()
            .baseUrl(DISCOVERY_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DiscoveryApi::class.java)
    }

    fun createLibraryApi(baseUrl: String): LibraryApi {
        // Ensure URL ends with /
        val safeUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        
        return Retrofit.Builder()
            .baseUrl(safeUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LibraryApi::class.java)
    }

    fun getLibraryApi(context: Context): LibraryApi {
        // Legacy method, might be deprecated or used as fallback
        val savedUrl = PreferencesManager.getLibraryUrl(context)
        return createLibraryApi(savedUrl)
    }
}
