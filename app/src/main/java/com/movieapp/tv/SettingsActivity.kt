package com.movieapp.tv

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.fragment.app.FragmentActivity

class SettingsActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Setup navbar navigation
        setupNavbar()

        // Set focus to Settings navbar item
        findViewById<LinearLayout>(R.id.nav_settings).requestFocus()
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

        bindNav(findViewById(R.id.nav_search)) {
            startActivity(Intent(this, SearchActivity::class.java))
            finish()
        }

        bindNav(findViewById(R.id.nav_settings)) {
            // Already on Settings page
        }
    }
}
