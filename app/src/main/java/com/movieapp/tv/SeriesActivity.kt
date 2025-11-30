package com.movieapp.tv

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.fragment.app.FragmentActivity
import android.view.KeyEvent
import android.view.View

class SeriesActivity : FragmentActivity() {

    private lateinit var seriesFragment: SeriesFragment
    private var currentFragment: androidx.fragment.app.Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_series)

        // Initialize with SeriesFragment
        seriesFragment = SeriesFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.series_fragment_container, seriesFragment)
            .commit()
        currentFragment = seriesFragment

        // Setup navbar navigation
        setupNavbar()

        // Focus on Series button initially
        findViewById<LinearLayout>(R.id.nav_series)?.requestFocus()
    }

    // Method to navigate to episode details
    fun navigateToEpisodes(folderPath: String, title: String) {
        val seriesDetailsFragment = SeriesDetailsFragment.newInstance(folderPath, title)
        supportFragmentManager.beginTransaction()
            .replace(R.id.series_fragment_container, seriesDetailsFragment)
            .addToBackStack(null)
            .commit()
        currentFragment = seriesDetailsFragment
    }

    // Handle back press to return to series list
    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
            currentFragment = seriesFragment
        } else {
            super.onBackPressed()
        }
    }



    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        // When user presses UP on D-pad from the first row of series/episodes, move focus to navbar
        if (event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            if (!isNavbarFocused()) {
                // Check if SeriesFragment can handle up key
                if (currentFragment is SeriesFragment && seriesFragment.handleUpKey()) {
                    findViewById<LinearLayout>(R.id.nav_series)?.requestFocus()
                    return true
                }
                // Check if it's SeriesDetailsFragment at top
                if (currentFragment is SeriesDetailsFragment) {
                    val detailsFragment = currentFragment as SeriesDetailsFragment
                    if (detailsFragment.handleUpKey()) {
                        findViewById<LinearLayout>(R.id.nav_series)?.requestFocus()
                        return true
                    }
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }

    private fun isNavbarFocused(): Boolean {
        val currentFocus = currentFocus ?: return false
        val navbar = findViewById<LinearLayout>(R.id.navbar) ?: return false
        return isViewDescendant(currentFocus, navbar)
    }

    private fun isViewDescendant(child: View, parent: View): Boolean {
        if (child == parent) return true
        val viewParent = child.parent
        return viewParent is View && isViewDescendant(viewParent, parent)
    }

    private fun setupNavbar() {
        findViewById<LinearLayout>(R.id.nav_home)?.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        findViewById<LinearLayout>(R.id.nav_movies)?.setOnClickListener {
            startActivity(Intent(this, LibraryActivity::class.java))
            finish()
        }

        findViewById<LinearLayout>(R.id.nav_series)?.setOnClickListener {
            // Already on Series page
        }

        findViewById<LinearLayout>(R.id.nav_search)?.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
            finish()
        }



        findViewById<LinearLayout>(R.id.nav_settings)?.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
            finish()
        }
    }
}
