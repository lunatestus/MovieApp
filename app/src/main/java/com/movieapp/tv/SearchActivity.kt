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
        fun bindNav(view: LinearLayout, action: () -> Unit) {
            view.setOnClickListener { action() }
            view.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    action()
                }
            }
        }

        bindNav(findViewById(R.id.nav_home)) {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        bindNav(findViewById(R.id.nav_movies)) {
            startActivity(Intent(this, LibraryActivity::class.java))
            finish()
        }

        bindNav(findViewById(R.id.nav_series)) {
            startActivity(Intent(this, SeriesActivity::class.java))
            finish()
        }

        bindNav(findViewById(R.id.nav_search)) {
            // Already on Search page
        }

        bindNav(findViewById(R.id.nav_settings)) {
            startActivity(Intent(this, SettingsActivity::class.java))
            finish()
        }
    }
}
