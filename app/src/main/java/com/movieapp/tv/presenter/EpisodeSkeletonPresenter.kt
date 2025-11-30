package com.movieapp.tv.presenter

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.leanback.widget.Presenter
import com.movieapp.tv.R

class EpisodeSkeletonPresenter : Presenter() {

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_episode_skeleton, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
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
        val animator = viewHolder.view.tag as? ObjectAnimator
        animator?.cancel()
    }
}
