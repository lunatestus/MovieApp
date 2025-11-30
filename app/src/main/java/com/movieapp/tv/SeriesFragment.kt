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
import com.movieapp.tv.presenter.SkeletonPresenter
import com.movieapp.tv.viewmodel.SeriesUiState
import com.movieapp.tv.viewmodel.SeriesViewModel
import kotlinx.coroutines.launch

class SeriesFragment : RowsSupportFragment() {

    private lateinit var viewModel: SeriesViewModel
    private lateinit var rowsAdapter: ArrayObjectAdapter
    
    private class SkeletonItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setupFragment()
        setupEventListeners()
        
        viewModel = ViewModelProvider(this)[SeriesViewModel::class.java]
        observeViewModel()
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val topPadding = (20 * resources.displayMetrics.density).toInt()
        
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
        rowsAdapter = ArrayObjectAdapter(LibraryListRowPresenter())
        adapter = rowsAdapter
    }

    private fun setupEventListeners() {
        onItemViewClickedListener = OnItemViewClickedListener { _, item, _, _ ->
            if (item is Movie) {
                if (item.isFolder) {
                    // Navigate to episodes within the same activity
                    val activity = requireActivity()
                    if (activity is SeriesActivity) {
                        activity.navigateToEpisodes(item.videoUrl ?: "", item.title)
                    }
                } else {
                    val intent = Intent(requireContext(), PlayerActivity::class.java)
                    intent.putExtra(PlayerActivity.EXTRA_VIDEO_URL, item.videoUrl)
                    intent.putExtra(PlayerActivity.EXTRA_VIDEO_TITLE, item.title)
                    startActivity(intent)
                }
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is SeriesUiState.Loading -> {
                        showSkeleton()
                    }
                    is SeriesUiState.Success -> {
                        updateAdapter(state.series)
                    }
                    is SeriesUiState.Error -> {
                        rowsAdapter.clear() 
                    }
                }
            }
        }
    }

    private fun showSkeleton() {
        rowsAdapter.clear()
        repeat(3) { index ->
            val skeletonAdapter = ArrayObjectAdapter(SkeletonPresenter())
            repeat(8) {
                skeletonAdapter.add(SkeletonItem())
            }
            val header = HeaderItem(index.toLong(), "")
            rowsAdapter.add(ListRow(header, skeletonAdapter))
        }
    }

    private fun updateAdapter(series: List<Movie>) {
        rowsAdapter.clear()
        
        val chunkSize = 8
        val chunks = series.chunked(chunkSize)
        
        chunks.forEachIndexed { index, chunk ->
            val movieAdapter = ArrayObjectAdapter(MovieCardPresenter())
            chunk.forEach { movieAdapter.add(it) }
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

    fun handleUpKey(): Boolean {
        val verticalGridView = findVerticalGridView(view)
        verticalGridView?.let { gridView ->
            if (gridView.selectedPosition == 0) {
                return true
            }
        }
        return false
    }
}
