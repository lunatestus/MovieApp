package com.movieapp.tv.presenter

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.leanback.widget.Presenter
import com.movieapp.tv.R

class SkeletonPresenter : Presenter() {

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_skeleton, parent, false)
        
        // Add margins to match MovieCardPresenter
        val res = parent.context.resources
        val cardWidth = res.getDimensionPixelSize(R.dimen.movie_card_width)
        val cardHeight = res.getDimensionPixelSize(R.dimen.movie_card_height)
        val marginInPixels = res.getDimensionPixelSize(R.dimen.movie_card_margin)
        val params = ViewGroup.MarginLayoutParams(cardWidth, cardHeight).apply {
            setMargins(marginInPixels, marginInPixels, marginInPixels, marginInPixels)
        }
        view.layoutParams = params
        
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
        // Start pulse animation
        val view = viewHolder.view
        val animator = ObjectAnimator.ofFloat(view, "alpha", 0.3f, 1.0f).apply {
            duration = 800
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
        }
        animator.start()
        view.tag = animator
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        // Stop animation
        val animator = viewHolder.view.tag as? ObjectAnimator
        animator?.cancel()
    }
}
