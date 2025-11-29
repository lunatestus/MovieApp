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
        findViewById<LinearLayout>(R.id.nav_home).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        findViewById<LinearLayout>(R.id.nav_library).setOnClickListener {
            startActivity(Intent(this, LibraryActivity::class.java))
            finish()
        }

        findViewById<LinearLayout>(R.id.nav_search).setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
            finish()
        }

        findViewById<LinearLayout>(R.id.nav_profile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            finish()
        }

        findViewById<LinearLayout>(R.id.nav_settings).setOnClickListener {
            // Already on Settings page
        }
    }
}
