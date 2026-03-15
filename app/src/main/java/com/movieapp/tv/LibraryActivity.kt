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

        // Focus on Movies button initially
        findViewById<LinearLayout>(R.id.nav_movies).requestFocus()
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        // When user presses UP on D-pad from the first row of library, move focus to navbar
        if (event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            if (!isNavbarFocused() && libraryFragment.handleUpKey()) {
                findViewById<LinearLayout>(R.id.nav_movies).requestFocus()
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
        fun bindNav(view: LinearLayout, action: () -> Unit) {
            view.setOnClickListener { action() }
            view.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    action()
                }
            }
        }

        bindNav(findViewById(R.id.nav_home)) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        bindNav(findViewById(R.id.nav_movies)) {
            // Already on Movies page
        }

        bindNav(findViewById(R.id.nav_search)) {
            startActivity(Intent(this, SearchActivity::class.java))
            finish()
        }

        bindNav(findViewById(R.id.nav_settings)) {
            startActivity(Intent(this, SettingsActivity::class.java))
            finish()
        }
    }
}
