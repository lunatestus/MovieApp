package com.movieapp.tv

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.leanback.app.RowsSupportFragment
import androidx.leanback.widget.*
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.movieapp.tv.model.Movie
import com.movieapp.tv.presenter.LibraryListRowPresenter
import com.movieapp.tv.presenter.MovieCardPresenter
import com.movieapp.tv.viewmodel.LibraryUiState
import com.movieapp.tv.viewmodel.LibraryViewModel
import kotlinx.coroutines.launch

class LibraryFragment : RowsSupportFragment() {

    private lateinit var viewModel: LibraryViewModel
    private lateinit var rowsAdapter: ArrayObjectAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setupFragment()
        setupEventListeners()
        
        viewModel = ViewModelProvider(this)[LibraryViewModel::class.java]
        observeViewModel()
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Set padding to match MainActivity alignment
        val navbarHeight = resources.getDimensionPixelSize(R.dimen.navbar_height)
        // Add extra padding to push content below navbar
        val topPadding = navbarHeight + (20 * resources.displayMetrics.density).toInt()
        
        // Access VerticalGridView to set padding and alignment
        findVerticalGridView(view)?.apply {
            setPadding(0, topPadding, 0, 0)
            clipToPadding = false
            windowAlignmentOffset = 0
            windowAlignmentOffsetPercent = 0f
            itemAlignmentOffset = 0
            itemAlignmentOffsetPercent = 0f
            windowAlignment = VerticalGridView.WINDOW_ALIGN_NO_EDGE
        }
    }

    private fun setupFragment() {
        // Use the library list row presenter for seamless grid look
        rowsAdapter = ArrayObjectAdapter(LibraryListRowPresenter())
        adapter = rowsAdapter
    }

    private fun setupEventListeners() {
        onItemViewClickedListener = OnItemViewClickedListener { _, item, _, _ ->
            if (item is Movie) {
                val intent = Intent(requireContext(), PlayerActivity::class.java)
                intent.putExtra(PlayerActivity.EXTRA_VIDEO_URL, item.videoUrl)
                intent.putExtra(PlayerActivity.EXTRA_VIDEO_TITLE, item.title)
                startActivity(intent)
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is LibraryUiState.Loading -> {
                        // Show loading if needed
                    }
                    is LibraryUiState.Success -> {
                        updateAdapter(state.movies)
                    }
                    is LibraryUiState.Error -> {
                        // Handle error
                    }
                }
            }
        }
    }

    private fun updateAdapter(movies: List<Movie>) {
        rowsAdapter.clear()
        
        // Chunk movies into rows of 6 to replicate homepage feel but with multiple rows
        val chunkSize = 6
        val chunks = movies.chunked(chunkSize)
        
        chunks.forEachIndexed { index, movieChunk ->
            val movieAdapter = ArrayObjectAdapter(MovieCardPresenter())
            movieChunk.forEach { movieAdapter.add(it) }
            
            // Use empty header or a simple number/category if needed
            // For library, usually we just want rows. Let's use empty header for clean look
            // or we can group by alphabet if sorted.
            // For now, just adding rows without titles or generic titles
            val header = HeaderItem(index.toLong(), "")
            rowsAdapter.add(ListRow(header, movieAdapter))
        }
    }
    
    private fun findVerticalGridView(view: View?): VerticalGridView? {
        if (view is VerticalGridView) return view
        if (view is android.view.ViewGroup) {
            for (i in 0 until view.childCount) {
                val child = view.getChildAt(i)
                val result = findVerticalGridView(child)
                if (result != null) return result
            }
        }
        return null
    }

    /**
     * Override to allow UP key to escape from the browse fragment back to navbar
     */
    fun handleUpKey(): Boolean {
        val verticalGridView = findVerticalGridView(view)
        
        // If we're at the first row (position 0), allow UP to escape to navbar
        verticalGridView?.let { gridView ->
            if (gridView.selectedPosition == 0) {
                return true // Signal that UP should be handled by parent
            }
        }
        return false
    }
}
