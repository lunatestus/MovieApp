package com.movieapp.tv.presenter

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.leanback.widget.Presenter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.movieapp.tv.R
import com.movieapp.tv.model.Movie

class MovieCardPresenter : Presenter() {
    
    companion object {
        private const val CARD_WIDTH = 220
        private const val CARD_HEIGHT = 330
        private const val FOCUSED_SCALE = 1.1f
        private const val UNFOCUSED_SCALE = 1.0f
        private const val ANIMATION_DURATION = 150L
    }
    
    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val imageView = ImageView(parent.context).apply {
            isFocusable = true
            isFocusableInTouchMode = true
            
            // Add margins for spacing between posters - reduced for tighter layout
            val marginInPixels = (3 * context.resources.displayMetrics.density).toInt()
            layoutParams = ViewGroup.MarginLayoutParams(CARD_WIDTH, CARD_HEIGHT).apply {
                setMargins(marginInPixels, marginInPixels, marginInPixels, marginInPixels)
            }
            
            scaleType = ImageView.ScaleType.CENTER_CROP
            
            // Add focus change listener for smooth animations
            setOnFocusChangeListener { view, hasFocus ->
                val scale = if (hasFocus) FOCUSED_SCALE else UNFOCUSED_SCALE
                val elevation = if (hasFocus) 16f else 0f
                
                view.animate()
                    .scaleX(scale)
                    .scaleY(scale)
                    .translationZ(elevation)
                    .setDuration(ANIMATION_DURATION)
                    .start()
            }
        }
        return ViewHolder(imageView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
        val movie = item as Movie
        val imageView = viewHolder.view as ImageView
        val context = imageView.context
        
        // Load poster with proper error handling and placeholder
        Glide.with(context)
            .load(movie.getPosterUrl())
            .placeholder(R.drawable.movie_placeholder)
            .error(R.drawable.movie_placeholder)
            .centerCrop()
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    // Keep placeholder on error
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }
            })
            .into(imageView)
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        val imageView = viewHolder.view as ImageView
        
        // Reset scale and elevation
        imageView.scaleX = UNFOCUSED_SCALE
        imageView.scaleY = UNFOCUSED_SCALE
        imageView.translationZ = 0f
        
        // Clear Glide to prevent memory leaks
        Glide.with(imageView.context).clear(imageView)
    }
}
