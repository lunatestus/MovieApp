package com.movieapp.tv

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.FragmentActivity

class SearchActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coming_soon)

        // Set page title
        findViewById<TextView>(R.id.page_title).text = "Search"

        // Setup navbar navigation
        setupNavbar()

        // Set focus to Search navbar item
        findViewById<LinearLayout>(R.id.nav_search).requestFocus()
    }

    private fun setupNavbar() {
        findViewById<LinearLayout>(R.id.nav_home).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        findViewById<LinearLayout>(R.id.nav_movies).setOnClickListener {
            startActivity(Intent(this, LibraryActivity::class.java))
            finish()
        }

        findViewById<LinearLayout>(R.id.nav_series).setOnClickListener {
            startActivity(Intent(this, SeriesActivity::class.java))
            finish()
        }

        findViewById<LinearLayout>(R.id.nav_search).setOnClickListener {
            // Already on Search page
        }



        findViewById<LinearLayout>(R.id.nav_settings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
            finish()
        }
    }
}
