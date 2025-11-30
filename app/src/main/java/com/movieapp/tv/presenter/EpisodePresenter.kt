package com.movieapp.tv.presenter

import android.graphics.Color
import android.view.ViewGroup
import android.widget.TextView
import androidx.leanback.widget.Presenter

class EpisodePresenter : Presenter() {

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(com.movieapp.tv.R.layout.item_episode, parent, false)
        
        // Set focus change listener on the root view
        view.setOnFocusChangeListener { v, hasFocus ->
            val titleView = v.findViewById<TextView>(com.movieapp.tv.R.id.episode_title)
            
            if (hasFocus) {
                titleView.setTextColor(Color.WHITE)
                titleView.isSelected = true 
            } else {
                titleView.setTextColor(Color.WHITE) // Keep bright white even when unfocused
                titleView.isSelected = false
            }
        }
        
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
        val movie = item as com.movieapp.tv.model.Movie
        val view = viewHolder.view
        val titleView = view.findViewById<TextView>(com.movieapp.tv.R.id.episode_title)
        val thumbnailView = view.findViewById<android.widget.ImageView>(com.movieapp.tv.R.id.episode_thumbnail)
        
        // Clean up title
        val cleanTitle = movie.title
            .replace(".mkv", "", ignoreCase = true)
            .replace(".mp4", "", ignoreCase = true)
            .replace(".avi", "", ignoreCase = true)
            .replace(".", " ")
            .replace("_", " ")
            .replace("  ", " ")
            .trim()
            
        titleView.text = cleanTitle

        // Load thumbnail (Backdrop -> Poster -> Placeholder)
        val imageUrl = if (!movie.backdropPath.isNullOrEmpty()) {
            movie.getBackdropUrl()
        } else {
            movie.getPosterUrl()
        }

        com.bumptech.glide.Glide.with(view.context)
            .load(imageUrl)
            .placeholder(com.movieapp.tv.R.drawable.movie_placeholder)
            .error(com.movieapp.tv.R.drawable.movie_placeholder)
            .centerCrop()
            .into(thumbnailView)
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        val view = viewHolder.view
        val thumbnailView = view.findViewById<android.widget.ImageView>(com.movieapp.tv.R.id.episode_thumbnail)
        com.bumptech.glide.Glide.with(view.context).clear(thumbnailView)
    }
}
