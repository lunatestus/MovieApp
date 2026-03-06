package com.movieapp.tv

import android.content.Context
import android.content.Intent
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
import com.movieapp.tv.model.Movie

class DetailsActivity : FragmentActivity() {

    private lateinit var posterImageView: ImageView
    private lateinit var backdropImageView: ImageView
    private lateinit var titleTextView: TextView
    private lateinit var overviewTextView: TextView
    private lateinit var ratingTextView: TextView
    private lateinit var releaseTextView: TextView
    private lateinit var backButton: Button
    private lateinit var playButton: Button
    private var playableUrl: String? = null

    companion object {
        private const val TAG = "DetailsActivity"
        const val EXTRA_TITLE = "movie_title"
        const val EXTRA_OVERVIEW = "movie_overview"
        const val EXTRA_BACKDROP = "movie_backdrop"
        const val EXTRA_POSTER = "movie_poster"
        const val EXTRA_RATING = "movie_rating"
        const val EXTRA_RELEASE = "movie_release"
        const val EXTRA_VIDEO_URL = "movie_video_url"

        fun createIntent(context: Context, movie: Movie, playableUrl: String? = null): Intent {
            return Intent(context, DetailsActivity::class.java).apply {
                putExtra(EXTRA_TITLE, movie.title)
                putExtra(EXTRA_OVERVIEW, movie.overview)
                putExtra(EXTRA_BACKDROP, movie.backdropPath)
                putExtra(EXTRA_POSTER, movie.posterPath)
                putExtra(EXTRA_RATING, movie.voteAverage)
                putExtra(EXTRA_RELEASE, movie.releaseDate)
                putExtra(EXTRA_VIDEO_URL, playableUrl ?: movie.videoUrl)
            }
        }
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
        playButton = findViewById(R.id.play_button)

        backButton.setOnClickListener {
            finish()
        }
        playButton.setOnClickListener {
            openPlayer()
        }
    }

    private fun loadMovieData() {
        try {
            val title = intent.getStringExtra(EXTRA_TITLE) ?: "Unknown Title"
            val overview = intent.getStringExtra(EXTRA_OVERVIEW) ?: "No overview available."
            val backdropPath = intent.getStringExtra(EXTRA_BACKDROP)
            val posterPath = intent.getStringExtra(EXTRA_POSTER)
            val rating = intent.getDoubleExtra(EXTRA_RATING, 0.0)
            val releaseDate = intent.getStringExtra(EXTRA_RELEASE) ?: "Unknown"
            playableUrl = intent.getStringExtra(EXTRA_VIDEO_URL)

            // Set text data
            titleTextView.text = title
            overviewTextView.text = overview
            ratingTextView.text = String.format("★ %.1f", rating)
            releaseTextView.text = if (releaseDate.length >= 4) releaseDate.take(4) else releaseDate
            updatePlayButtonState()

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

    private fun updatePlayButtonState() {
        val isPlayable = !playableUrl.isNullOrBlank()
        playButton.isEnabled = isPlayable
        playButton.alpha = if (isPlayable) 1f else 0.5f
        playButton.text = getString(
            if (isPlayable) R.string.play_movie else R.string.play_unavailable
        )
    }

    private fun openPlayer() {
        val videoUrl = playableUrl ?: return
        val title = titleTextView.text?.toString().orEmpty()
        val intent = Intent(this, PlayerActivity::class.java).apply {
            putExtra(PlayerActivity.EXTRA_VIDEO_URL, videoUrl)
            putExtra(PlayerActivity.EXTRA_VIDEO_TITLE, title)
        }
        startActivity(intent)
    }

    private fun setupFocusHandling() {
        val buttonFocusListener = View.OnFocusChangeListener { view, hasFocus ->
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

        backButton.onFocusChangeListener = buttonFocusListener
        playButton.onFocusChangeListener = buttonFocusListener

        if (playButton.isEnabled) {
            playButton.requestFocus()
        } else {
            backButton.requestFocus()
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
