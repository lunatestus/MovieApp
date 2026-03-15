package com.movieapp.tv

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.movieapp.tv.utils.PreferencesManager

class SettingsActivity : FragmentActivity() {

    private lateinit var etLibraryUrl: EditText
    private lateinit var btnSave: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        etLibraryUrl = findViewById(R.id.et_library_url)
        btnSave = findViewById(R.id.btn_save_settings)

        // Load current URL
        val currentUrl = PreferencesManager.getLibraryUrl(this)
        etLibraryUrl.setText(currentUrl)

        btnSave.setOnClickListener {
            saveSettings()
        }

        // Setup navbar navigation
        setupNavbar()

        // Set focus to Settings navbar item
        findViewById<LinearLayout>(R.id.nav_settings).requestFocus()
    }

    private fun saveSettings() {
        val url = etLibraryUrl.text.toString().trim()
        if (url.isEmpty()) {
            Toast.makeText(this, "Please enter a valid URL", Toast.LENGTH_SHORT).show()
            return
        }

        PreferencesManager.saveLibraryUrl(this, url)
        Toast.makeText(this, "Settings saved successfully", Toast.LENGTH_SHORT).show()
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
