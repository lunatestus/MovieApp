package com.movieapp.tv.presenter

import android.view.ViewGroup
import androidx.leanback.widget.ListRowPresenter
import androidx.leanback.widget.RowPresenter
import com.movieapp.tv.R

/**
 * Custom ListRowPresenter that reduces spacing between rows and sections.
 * Overrides default Leanback spacing which adds massive padding.
 */
class CustomListRowPresenter : ListRowPresenter() {

    init {
        // Disable shadow to reduce spacing
        shadowEnabled = false
        
        // Reduce header height and spacing
        selectEffectEnabled = false
    }

    override fun initializeRowViewHolder(holder: RowPresenter.ViewHolder) {
        super.initializeRowViewHolder(holder)
        
        val res = holder.view.context.resources
        
        // Apply reduced margins to tighten gaps between rows
        val lp = holder.view.layoutParams as? ViewGroup.MarginLayoutParams
        lp?.apply {
            topMargin = res.getDimensionPixelSize(R.dimen.row_margin_top)
            bottomMargin = res.getDimensionPixelSize(R.dimen.row_margin_bottom)
            holder.view.layoutParams = this
        }
        
        // Adjust header height and margins
        holder.headerViewHolder?.view?.let { headerView ->
            val headerLp = headerView.layoutParams
            headerLp?.height = 0
            headerView.layoutParams = headerLp
            headerView.visibility = android.view.View.GONE

            (headerLp as? ViewGroup.MarginLayoutParams)?.apply {
                topMargin = 0
                bottomMargin = 0
            }
        }
    }
}
