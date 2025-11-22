package com.movieapp.tv.utils

import android.content.Context
import android.content.SharedPreferences

object PreferencesManager {
    private const val PREF_NAME = "movieapp_prefs"
    private const val KEY_LIBRARY_URL = "library_url"
    private const val DEFAULT_LIBRARY_URL = "https://yashkushwahayt--modal-video-uploader-flask-app.modal.run/"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun getLibraryUrl(context: Context): String {
        return getPrefs(context).getString(KEY_LIBRARY_URL, DEFAULT_LIBRARY_URL) ?: DEFAULT_LIBRARY_URL
    }

    fun saveLibraryUrl(context: Context, url: String) {
        getPrefs(context).edit().putString(KEY_LIBRARY_URL, url).apply()
    }
}
