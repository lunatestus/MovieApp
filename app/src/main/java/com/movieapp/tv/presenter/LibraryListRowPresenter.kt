package com.movieapp.tv.presenter

import android.view.View
import android.view.ViewGroup
import androidx.leanback.widget.ListRowPresenter
import androidx.leanback.widget.RowPresenter
import com.movieapp.tv.R

/**
 * Custom ListRowPresenter for Library that completely hides header space
 * to create a seamless grid-like appearance with rows.
 */
class LibraryListRowPresenter : ListRowPresenter() {

    init {
        shadowEnabled = false
        selectEffectEnabled = false
    }

    override fun initializeRowViewHolder(holder: RowPresenter.ViewHolder) {
        super.initializeRowViewHolder(holder)
        
        val res = holder.view.context.resources
        
        // Apply reduced margins for tight row packing
        val lp = holder.view.layoutParams as? ViewGroup.MarginLayoutParams
        lp?.apply {
            topMargin = 0
            bottomMargin = res.getDimensionPixelSize(R.dimen.row_margin_bottom)
            holder.view.layoutParams = this
        }
        
        // Completely hide the header view
        holder.headerViewHolder?.view?.let { headerView ->
            headerView.visibility = View.GONE
            val headerLp = headerView.layoutParams
            headerLp?.height = 0
            headerView.layoutParams = headerLp
        }
    }
}
