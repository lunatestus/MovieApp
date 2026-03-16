package com.movieapp.tv

import android.os.Bundle
import android.view.View
import androidx.leanback.app.RowsSupportFragment
import androidx.leanback.widget.*
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.movieapp.tv.model.Movie
import com.movieapp.tv.presenter.LibraryListRowPresenter
import com.movieapp.tv.presenter.MovieCardPresenter
import com.movieapp.tv.presenter.SkeletonPresenter
import com.movieapp.tv.viewmodel.LibraryUiState
import com.movieapp.tv.viewmodel.LibraryViewModel
import kotlinx.coroutines.launch

class LibraryFragment : RowsSupportFragment() {

    private lateinit var viewModel: LibraryViewModel
    private lateinit var rowsAdapter: ArrayObjectAdapter
    
    // Simple placeholder object for skeleton items
    private class SkeletonItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setupFragment()
        viewModel = ViewModelProvider(this)[LibraryViewModel::class.java]
        observeViewModel()
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Set padding to match visual preference
        // Since fragment is constrained below navbar, we just need a small offset
        val topPadding = (20 * resources.displayMetrics.density).toInt()
        
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

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is LibraryUiState.Loading -> {
                        showSkeleton()
                    }
                    is LibraryUiState.Success -> {
                        updateAdapter(state.movies)
                    }
                    is LibraryUiState.Error -> {
                        // Handle error - maybe clear or show error message
                        rowsAdapter.clear() 
                    }
                }
            }
        }
    }

    private fun showSkeleton() {
        rowsAdapter.clear()
        
        // Create 3 rows of skeleton items
        repeat(3) { index ->
            val skeletonAdapter = ArrayObjectAdapter(SkeletonPresenter())
            repeat(8) { // 8 items per row
                skeletonAdapter.add(SkeletonItem())
            }
            val header = HeaderItem(index.toLong(), "")
            rowsAdapter.add(ListRow(header, skeletonAdapter))
        }
    }

    private fun updateAdapter(movies: List<Movie>) {
        rowsAdapter.clear()
        
        // Chunk movies into rows of 8 to replicate homepage feel but with multiple rows
        val chunkSize = 8
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
