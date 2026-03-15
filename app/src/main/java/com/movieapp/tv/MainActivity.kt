package com.movieapp.tv

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.KeyEvent
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.leanback.widget.*
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.movieapp.tv.model.Movie
import com.movieapp.tv.repository.LibraryRepository
import com.movieapp.tv.presenter.CustomListRowPresenter
import com.movieapp.tv.presenter.MovieCardPresenter
import com.movieapp.tv.viewmodel.MainUiState
import com.movieapp.tv.viewmodel.MainViewModel
import com.movieapp.tv.databinding.ActivityMainBinding
import com.movieapp.tv.databinding.LayoutHeroSectionBinding
import com.movieapp.tv.databinding.NavbarLayoutBinding
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers

import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

class MainActivity : FragmentActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var heroBinding: LayoutHeroSectionBinding
    private lateinit var navbarBinding: NavbarLayoutBinding
    private lateinit var browseSupportFragment: CustomBrowseFragment
    private lateinit var viewModel: MainViewModel
    private var rowsAdapter: ArrayObjectAdapter? = null

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        browseSupportFragment = supportFragmentManager
            .findFragmentById(R.id.browse_fragment) as CustomBrowseFragment

        heroBinding = binding.heroSection
        navbarBinding = NavbarLayoutBinding.bind(binding.root.findViewById(R.id.navbar))

        setupUI()
        setupNavbar()
        observeViewModel()

        // Focus on Home button initially
        navbarBinding.navHome.requestFocus()

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

            onItemViewClickedListener = null
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
        heroBinding.heroMetadataContainer.visibility = View.VISIBLE
        heroBinding.heroTitle.text = movie.title
        heroBinding.heroDescription.text = movie.overview
        heroBinding.heroRating.text = movie.getFormattedRating()
        heroBinding.heroYear.text = movie.getFormattedYear()
        heroBinding.heroLanguage.text = movie.getFormattedLanguage()

        // Load background image
        val backdropUrl = movie.getBackdropUrl()
        if (backdropUrl.isBlank()) {
            heroBinding.heroBackground.setImageResource(R.drawable.backdrop_placeholder)
        } else {
            Glide.with(this)
                .load(backdropUrl)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(heroBinding.heroBackground)
        }
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
        val firstItem = state.nowPlaying.firstOrNull()
            ?: state.popular.firstOrNull()
            ?: state.topRated.firstOrNull()
            ?: state.upcoming.firstOrNull()

        if (firstItem != null) {
            updateHeroSection(firstItem)
        } else {
            heroBinding.heroMetadataContainer.visibility = View.GONE
            heroBinding.heroTitle.text = ""
            heroBinding.heroDescription.text = ""
            heroBinding.heroBackground.setImageResource(R.drawable.backdrop_placeholder)
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
        fun bindNav(view: View, action: () -> Unit) {
            view.setOnClickListener { action() }
            view.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    action()
                }
            }
        }

        bindNav(navbarBinding.navHome) {
            // Already on home - do nothing
        }

        bindNav(navbarBinding.navMovies) {
            startActivity(Intent(this, LibraryActivity::class.java))
            finish()
        }

        bindNav(navbarBinding.navSearch) {
            startActivity(Intent(this, SearchActivity::class.java))
            finish()
        }

        bindNav(navbarBinding.navSettings) {
            startActivity(Intent(this, SettingsActivity::class.java))
            finish()
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        // When user presses UP on D-pad from the first row, move focus to navbar
        if (event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            if (!isNavbarFocused() && browseSupportFragment.handleUpKey()) {
                navbarBinding.navHome.requestFocus()
                return true
            }
        }
        return super.dispatchKeyEvent(event)
    }

    private fun isNavbarFocused(): Boolean {
        val currentFocus = currentFocus ?: return false
        return isViewDescendant(currentFocus, navbarBinding.root)
    }

    private fun isViewDescendant(child: View, parent: View): Boolean {
        if (child == parent) return true
        val viewParent = child.parent
        return viewParent is View && isViewDescendant(viewParent, parent)
    }
}
