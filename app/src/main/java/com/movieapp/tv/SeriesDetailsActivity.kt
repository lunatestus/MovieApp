package com.movieapp.tv

import android.os.Bundle
import android.view.KeyEvent
import androidx.fragment.app.FragmentActivity

class SeriesDetailsActivity : FragmentActivity() {

    companion object {
        const val EXTRA_FOLDER_PATH = "extra_folder_path"
        const val EXTRA_TITLE = "extra_title"
    }

    private lateinit var fragment: SeriesDetailsFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_series_details)

        val folderPath = intent.getStringExtra(EXTRA_FOLDER_PATH) ?: return
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Series Details"

        if (savedInstanceState == null) {
            fragment = SeriesDetailsFragment.newInstance(folderPath, title)
            supportFragmentManager.beginTransaction()
                .replace(R.id.series_details_fragment_container, fragment)
                .commit()
        } else {
            fragment = supportFragmentManager.findFragmentById(R.id.series_details_fragment_container) as SeriesDetailsFragment
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        // Handle BACK key to finish activity
        if (event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_BACK) {
            finish()
            return true
        }
        return super.dispatchKeyEvent(event)
    }
}
