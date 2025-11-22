package com.movieapp.tv

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

class DetailsActivity : FragmentActivity() {

    private lateinit var posterImageView: ImageView
    private lateinit var backdropImageView: ImageView
    private lateinit var titleTextView: TextView
    private lateinit var overviewTextView: TextView
    private lateinit var ratingTextView: TextView
    private lateinit var releaseTextView: TextView
    private lateinit var backButton: Button

    companion object {
        private const val TAG = "DetailsActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        initializeViews()
        loadMovieData()
        setupFocusHandling()
    }

    private fun initializeViews() {
        posterImageView = findViewById(R.id.movie_poster)
        backdropImageView = findViewById(R.id.movie_backdrop)
        titleTextView = findViewById(R.id.movie_title)
        overviewTextView = findViewById(R.id.movie_overview)
        ratingTextView = findViewById(R.id.movie_rating)
        releaseTextView = findViewById(R.id.movie_release)
        backButton = findViewById(R.id.back_button)

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun loadMovieData() {
        try {
            val title = intent.getStringExtra("movie_title") ?: "Unknown Title"
            val overview = intent.getStringExtra("movie_overview") ?: "No overview available."
            val backdropPath = intent.getStringExtra("movie_backdrop")
            val posterPath = intent.getStringExtra("movie_poster")
            val rating = intent.getDoubleExtra("movie_rating", 0.0)
            val releaseDate = intent.getStringExtra("movie_release") ?: "Unknown"

            // Set text data
            titleTextView.text = title
            overviewTextView.text = overview
            ratingTextView.text = String.format("★ %.1f", rating)
            releaseTextView.text = if (releaseDate.length >= 4) releaseDate.take(4) else releaseDate

            // Load backdrop image
            if (!backdropPath.isNullOrEmpty()) {
                val backdropUrl = "https://image.tmdb.org/t/p/original$backdropPath"
                Glide.with(this)
                    .load(backdropUrl)
                    .centerCrop()
                    .placeholder(R.drawable.backdrop_placeholder)
                    .error(R.drawable.backdrop_placeholder)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>,
                            isFirstResource: Boolean
                        ): Boolean {
                            Log.e(TAG, "Failed to load backdrop", e)
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable,
                            model: Any,
                            target: Target<Drawable>,
                            dataSource: DataSource,
                            isFirstResource: Boolean
                        ): Boolean {
                            return false
                        }
                    })
                    .into(backdropImageView)
            }

            // Load poster image
            if (!posterPath.isNullOrEmpty()) {
                val posterUrl = "https://image.tmdb.org/t/p/w500$posterPath"
                Glide.with(this)
                    .load(posterUrl)
                    .centerCrop()
                    .placeholder(R.drawable.movie_placeholder)
                    .error(R.drawable.movie_placeholder)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>,
                            isFirstResource: Boolean
                        ): Boolean {
                            Log.e(TAG, "Failed to load poster", e)
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable,
                            model: Any,
                            target: Target<Drawable>,
                            dataSource: DataSource,
                            isFirstResource: Boolean
                        ): Boolean {
                            return false
                        }
                    })
                    .into(posterImageView)
            }

            Log.d(TAG, "Movie details loaded: $title")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading movie data", e)
        }
    }

    private fun setupFocusHandling() {
        // Request focus on back button initially
        backButton.requestFocus()

        // Add focus change listeners for visual feedback
        backButton.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                view.animate()
                    .scaleX(1.1f)
                    .scaleY(1.1f)
                    .setDuration(150)
                    .start()
            } else {
                view.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(150)
                    .start()
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // Handle back button press
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clear Glide to prevent memory leaks
        Glide.with(this).clear(posterImageView)
        Glide.with(this).clear(backdropImageView)
    }
}
