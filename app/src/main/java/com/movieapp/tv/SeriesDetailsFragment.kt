package com.movieapp.tv

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.leanback.app.RowsSupportFragment
import androidx.leanback.widget.*
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.movieapp.tv.model.Movie
import com.movieapp.tv.presenter.EpisodePresenter
import com.movieapp.tv.presenter.EpisodeSkeletonPresenter
import com.movieapp.tv.presenter.LibraryListRowPresenter
import com.movieapp.tv.viewmodel.SeriesDetailsUiState
import com.movieapp.tv.viewmodel.SeriesDetailsViewModel
import kotlinx.coroutines.launch

class SeriesDetailsFragment : RowsSupportFragment() {

    private lateinit var viewModel: SeriesDetailsViewModel
    private lateinit var rowsAdapter: ArrayObjectAdapter
    private var folderPath: String = ""
    private var seriesTitle: String = ""

    companion object {
        fun newInstance(folderPath: String, title: String): SeriesDetailsFragment {
            val fragment = SeriesDetailsFragment()
            val args = Bundle()
            args.putString(SeriesDetailsActivity.EXTRA_FOLDER_PATH, folderPath)
            args.putString(SeriesDetailsActivity.EXTRA_TITLE, title)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        folderPath = arguments?.getString(SeriesDetailsActivity.EXTRA_FOLDER_PATH) ?: ""
        seriesTitle = arguments?.getString(SeriesDetailsActivity.EXTRA_TITLE) ?: ""

        setupFragment()
        setupEventListeners()
        
        viewModel = ViewModelProvider(this)[SeriesDetailsViewModel::class.java]
        viewModel.loadEpisodes(folderPath)
        observeViewModel()
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Set padding for visual preference
        val topPadding = (20 * resources.displayMetrics.density).toInt()
        
        // Access VerticalGridView to set padding and alignment
        findVerticalGridView(view)?.apply {
            setPadding(0, topPadding, 0, 0)  // No left/right padding
            clipToPadding = false
            windowAlignmentOffset = 0
            windowAlignmentOffsetPercent = 0f
            itemAlignmentOffset = 0
            itemAlignmentOffsetPercent = 0f
            windowAlignment = VerticalGridView.WINDOW_ALIGN_NO_EDGE
        }
    }

    private fun setupFragment() {
        // Use LibraryListRowPresenter for seamless grid look
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
                    is SeriesDetailsUiState.Loading -> {
                        showSkeletons()
                    }
                    is SeriesDetailsUiState.Success -> {
                        updateAdapter(state.episodes)
                    }
                    is SeriesDetailsUiState.Error -> {
                        android.util.Log.e("SeriesDetailsFragment", "Error loading episodes: ${state.message}")
                        android.widget.Toast.makeText(requireContext(), state.message, android.widget.Toast.LENGTH_LONG).show()
                        rowsAdapter.clear()
                    }
                }
            }
        }
    }

    private fun showSkeletons() {
        rowsAdapter.clear()
        
        // Create 3 rows of skeleton items (4 per row)
        repeat(3) { index ->
            val skeletonAdapter = ArrayObjectAdapter(EpisodeSkeletonPresenter())
            repeat(4) { // 4 items per row
                skeletonAdapter.add(Any())
            }
            val header = HeaderItem(index.toLong(), "")
            rowsAdapter.add(ListRow(header, skeletonAdapter))
        }
    }

    private fun updateAdapter(episodes: List<Movie>) {
        rowsAdapter.clear()
        
        if (episodes.isEmpty()) {
            android.util.Log.e("SeriesDetailsFragment", "No episodes to display!")
            return
        }
        
        android.util.Log.d("SeriesDetailsFragment", "Displaying ${episodes.size} episodes")
        
        // Chunk episodes into rows of 4
        val chunkSize = 4
        val chunks = episodes.chunked(chunkSize)
        
        chunks.forEachIndexed { index, episodeChunk ->
            val episodeAdapter = ArrayObjectAdapter(EpisodePresenter())
            episodeChunk.forEach { episodeAdapter.add(it) }
            
            // Use empty header for clean look
            val header = HeaderItem(index.toLong(), "")
            rowsAdapter.add(ListRow(header, episodeAdapter))
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
     * Override to allow UP key to escape from the fragment back to navbar
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
