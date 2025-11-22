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
    
    companion object {
        // Offset to ensure rows start at the correct position within the bottom half of the split screen
        private const val ROW_ALIGNMENT_OFFSET_DP = 20
    }

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
                val navbarHeight = resources.getDimensionPixelSize(R.dimen.navbar_height)
                gridView.setPadding(0, navbarHeight, 0, 0)
                gridView.clipToPadding = false

                // Configure window alignment for perfect row positioning
                // windowAlignmentOffset: Distance from top of screen where items should align
                // Since the fragment starts at the middle of the screen, we want items to align
                // near the top of the fragment container
                val offsetPixels = (ROW_ALIGNMENT_OFFSET_DP * resources.displayMetrics.density).toInt()
                gridView.windowAlignmentOffset = offsetPixels
                
                // Disable percentage-based alignment to use fixed offset
                gridView.windowAlignmentOffsetPercent = VerticalGridView.WINDOW_ALIGN_OFFSET_PERCENT_DISABLED
                
                // Configure item alignment
                // itemAlignmentOffset: Position on the item to align (0 = top of item)
                gridView.itemAlignmentOffset = 0
                
                // itemAlignmentOffsetPercent: Use top of item for alignment
                gridView.itemAlignmentOffsetPercent = 0f
                
                // Set window alignment to ensure proper positioning
                gridView.windowAlignment = VerticalGridView.WINDOW_ALIGN_LOW_EDGE
                
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
