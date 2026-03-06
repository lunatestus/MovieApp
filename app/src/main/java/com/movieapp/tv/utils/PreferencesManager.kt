package com.movieapp.tv.utils

import android.content.Context
import android.content.SharedPreferences
import com.movieapp.tv.BuildConfig

object PreferencesManager {
    private const val PREF_NAME = "movieapp_prefs"
    private const val KEY_LIBRARY_URL = "library_url"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun getLibraryUrl(context: Context): String {
        return getPrefs(context).getString(KEY_LIBRARY_URL, BuildConfig.DEFAULT_LIBRARY_URL)
            ?: BuildConfig.DEFAULT_LIBRARY_URL
    }

    fun saveLibraryUrl(context: Context, url: String) {
        getPrefs(context).edit().putString(KEY_LIBRARY_URL, url).apply()
    }
}
