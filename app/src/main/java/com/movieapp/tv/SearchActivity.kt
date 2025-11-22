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
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        findViewById<LinearLayout>(R.id.nav_library).setOnClickListener {
            startActivity(Intent(this, LibraryActivity::class.java))
            finish()
        }

        findViewById<LinearLayout>(R.id.nav_search).setOnClickListener {
            // Already on Search page
        }

        findViewById<LinearLayout>(R.id.nav_profile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            finish()
        }

        findViewById<LinearLayout>(R.id.nav_settings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
            finish()
        }
    }
}
