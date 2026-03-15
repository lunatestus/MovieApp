package com.movieapp.tv

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import androidx.leanback.app.RowsSupportFragment
import androidx.leanback.widget.VerticalGridView

/**
 * Custom RowsSupportFragment that configures perfect row alignment for TV D-pad navigation.
 * This ensures that when scrolling with the D-pad, section headers remain visible and
 * rows align perfectly on screen.
 */
class CustomBrowseFragment : RowsSupportFragment() {

    private val handler = Handler(Looper.getMainLooper())
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Make background transparent to show hero image
        view.background = null
        
        // Post configuration to ensure views are fully initialized
        handler.post {
            configureRowsAlignment()
        }
    }

    private fun configureRowsAlignment() {
        try {
            // Access the VerticalGridView directly from the view
            val verticalGridView = findVerticalGridView(view)
            
            verticalGridView?.let { gridView ->
                // Force padding to remove massive default padding from BrowseSupportFragment
                val extraTopPadding = resources.getDimensionPixelSize(R.dimen.browse_rows_top_padding_extra)
                val extraBottomPadding = resources.getDimensionPixelSize(R.dimen.browse_rows_bottom_padding_extra)
                gridView.setPadding(0, extraTopPadding, 0, extraBottomPadding)
                gridView.clipToPadding = false

                // Configure window alignment for perfect row positioning
                // Keep items aligned within the fragment bounds to avoid header clipping
                gridView.windowAlignmentOffset = 0
                
                // Disable percentage-based alignment to use fixed offset
                gridView.windowAlignmentOffsetPercent = VerticalGridView.WINDOW_ALIGN_OFFSET_PERCENT_DISABLED
                
                // Configure item alignment
                // itemAlignmentOffset: Position on the item to align (0 = top of item)
                gridView.itemAlignmentOffset = 0
                
                // itemAlignmentOffsetPercent: Use top of item for alignment
                gridView.itemAlignmentOffsetPercent = 0f
                
                // Set window alignment to ensure proper positioning
                gridView.windowAlignment = VerticalGridView.WINDOW_ALIGN_NO_EDGE
                
                // Set vertical spacing between rows
                gridView.verticalSpacing = resources.getDimensionPixelSize(R.dimen.browse_rows_vertical_spacing)
            }
        } catch (e: Exception) {
            // Silently handle any exceptions during configuration
            e.printStackTrace()
        }
    }

    /**
     * Recursively searches for VerticalGridView in the view hierarchy
     */
    private fun findVerticalGridView(view: View?): VerticalGridView? {
        if (view == null) return null
        
        if (view is VerticalGridView) {
            return view
        }
        
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val found = findVerticalGridView(view.getChildAt(i))
                if (found != null) return found
            }
        }
        
        return null
    }

    override fun onResume() {
        super.onResume()
        // Reapply alignment settings when fragment resumes
        handler.post {
            configureRowsAlignment()
        }
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
