package com.movieapp.tv

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.fragment.app.FragmentActivity

import android.view.KeyEvent
import android.view.View

class LibraryActivity : FragmentActivity() {

    private lateinit var libraryFragment: LibraryFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_library)

        libraryFragment = supportFragmentManager.findFragmentById(R.id.library_fragment) as LibraryFragment

        // Setup navbar navigation
        setupNavbar()

        // Focus on Library button initially
        findViewById<LinearLayout>(R.id.nav_library).requestFocus()
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        // When user presses UP on D-pad from the first row of library, move focus to navbar
        if (event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            if (!isNavbarFocused() && libraryFragment.handleUpKey()) {
                findViewById<LinearLayout>(R.id.nav_library).requestFocus()
                return true
            }
        }
        return super.dispatchKeyEvent(event)
    }

    private fun isNavbarFocused(): Boolean {
        val currentFocus = currentFocus ?: return false
        val navbar = findViewById<LinearLayout>(R.id.navbar)
        return isViewDescendant(currentFocus, navbar)
    }

    private fun isViewDescendant(child: View, parent: View): Boolean {
        if (child == parent) return true
        val viewParent = child.parent
        return viewParent is View && isViewDescendant(viewParent, parent)
    }

    private fun setupNavbar() {
        findViewById<LinearLayout>(R.id.nav_home).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        findViewById<LinearLayout>(R.id.nav_library).setOnClickListener {
            // Already on Library page
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
            startActivity(Intent(this, SettingsActivity::class.java))
            finish()
        }
    }
}
