package com.movieapp.tv

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.*
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.movieapp.tv.model.Movie
import com.movieapp.tv.repository.LibraryRepository
import com.movieapp.tv.presenter.CustomListRowPresenter
import com.movieapp.tv.presenter.MovieCardPresenter
import com.movieapp.tv.viewmodel.MainUiState
import com.movieapp.tv.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers

import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

class MainActivity : FragmentActivity() {

    private lateinit var browseSupportFragment: CustomBrowseFragment
    private lateinit var viewModel: MainViewModel
    private var rowsAdapter: ArrayObjectAdapter? = null

    // Hero Section Views
    private lateinit var heroTitle: TextView
    private lateinit var heroDescription: TextView
    private lateinit var heroRating: TextView
    private lateinit var heroYear: TextView
    private lateinit var heroLanguage: TextView
    private lateinit var heroBackground: ImageView
    private lateinit var heroMetadataContainer: View

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        browseSupportFragment = supportFragmentManager
            .findFragmentById(R.id.browse_fragment) as CustomBrowseFragment

        // Initialize Hero Views
        heroTitle = findViewById(R.id.hero_title)
        heroDescription = findViewById(R.id.hero_description)
        heroRating = findViewById(R.id.hero_rating)
        heroYear = findViewById(R.id.hero_year)
        heroLanguage = findViewById(R.id.hero_language)
        heroBackground = findViewById(R.id.hero_background)
        heroMetadataContainer = findViewById(R.id.hero_metadata_container)

        setupUI()
        setupNavbar()
        observeViewModel()

        // Focus on Home button initially
        findViewById<LinearLayout>(R.id.nav_home).requestFocus()

        // Preload Library content
        preloadLibrary()
    }

    private fun preloadLibrary() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "Starting library preload...")
                LibraryRepository(applicationContext).getLibraryMovies()
                Log.d(TAG, "Library preload completed")
            } catch (e: Exception) {
                Log.e(TAG, "Library preload failed", e)
            }
        }
    }

    private fun setupUI() {
        browseSupportFragment.apply {
            // Configure window alignment for perfect row scrolling
            setSelectedPosition(0, true)

            // Listen for item selection to update Hero Section
            onItemViewSelectedListener = OnItemViewSelectedListener { _, item, _, _ ->
                if (item is Movie) {
                    updateHeroSection(item)
                }
            }

            onItemViewClickedListener = OnItemViewClickedListener { _, item, _, _ ->
                if (item is Movie) {
                    startActivity(DetailsActivity.createIntent(this@MainActivity, item, findPlayableUrl(item)))
                }
            }
        }

        // Initialize adapter
        rowsAdapter = ArrayObjectAdapter(CustomListRowPresenter())
        browseSupportFragment.adapter = rowsAdapter
    }

    private fun findPlayableUrl(movie: Movie): String? {
        return LibraryRepository(applicationContext)
            .getCachedMovies()
            .firstOrNull { it.id == movie.id && !it.videoUrl.isNullOrBlank() }
            ?.videoUrl
    }

    private fun updateHeroSection(movie: Movie) {
        heroMetadataContainer.visibility = View.VISIBLE
        heroTitle.text = movie.title
        heroDescription.text = movie.overview
        heroRating.text = movie.getFormattedRating()
        heroYear.text = movie.getFormattedYear()
        heroLanguage.text = movie.getFormattedLanguage()

        // Load background image
        Glide.with(this)
            .load(movie.getBackdropUrl())
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(heroBackground)
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is MainUiState.Loading -> {
                        // Optional: Show loading indicator
                    }
                    is MainUiState.Success -> {
                        updateAdapter(state)
                    }
                    is MainUiState.Error -> {
                        showError(state.message)
                    }
                }
            }
        }
    }

    private fun updateAdapter(state: MainUiState.Success) {
        val adapter = rowsAdapter ?: return
        adapter.clear()

        if (state.nowPlaying.isNotEmpty()) {
            addMovieRow(adapter, 0, "Now Playing", state.nowPlaying)
        }
        if (state.popular.isNotEmpty()) {
            addMovieRow(adapter, 1, "Popular", state.popular)
        }
        if (state.topRated.isNotEmpty()) {
            addMovieRow(adapter, 2, "Top Rated", state.topRated)
        }
        if (state.upcoming.isNotEmpty()) {
            addMovieRow(adapter, 3, "Upcoming", state.upcoming)
        }
        if (state.popularTv.isNotEmpty()) {
            addMovieRow(adapter, 4, "Popular TV Shows", state.popularTv)
        }
        if (state.topRatedTv.isNotEmpty()) {
            addMovieRow(adapter, 5, "Top Rated TV Shows", state.topRatedTv)
        }
    }

    private fun addMovieRow(
        rowsAdapter: ArrayObjectAdapter,
        id: Long,
        title: String,
        movies: List<Movie>
    ) {
        val movieAdapter = ArrayObjectAdapter(MovieCardPresenter())
        movies.forEach { movieAdapter.add(it) }
        val header = HeaderItem(id, title)
        rowsAdapter.add(ListRow(header, movieAdapter))
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        Log.e(TAG, message)
    }

    private fun setupNavbar() {
        findViewById<LinearLayout>(R.id.nav_home).setOnClickListener {
            // Already on home - do nothing
        }

        findViewById<LinearLayout>(R.id.nav_movies).setOnClickListener {
            startActivity(Intent(this, LibraryActivity::class.java))
            finish()
        }

        findViewById<LinearLayout>(R.id.nav_series).setOnClickListener {
            startActivity(Intent(this, SeriesActivity::class.java))
            finish()
        }

        findViewById<LinearLayout>(R.id.nav_search).setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
            finish()
        }



        findViewById<LinearLayout>(R.id.nav_settings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
            finish()
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        // When user presses UP on D-pad from the first row, move focus to navbar
        if (event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            if (!isNavbarFocused() && browseSupportFragment.handleUpKey()) {
                findViewById<LinearLayout>(R.id.nav_home).requestFocus()
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
}
