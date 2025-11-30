package com.movieapp.tv.presenter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.movieapp.tv.R
import com.movieapp.tv.model.Movie

class FilePresenter(private val onItemClick: (Movie) -> Unit) : 
    RecyclerView.Adapter<FilePresenter.FileViewHolder>() {

    private var files: List<Movie> = emptyList()

    fun setFiles(newFiles: List<Movie>) {
        files = newFiles
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_file, parent, false)
        return FileViewHolder(view)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        holder.bind(files[position], onItemClick)
    }

    override fun getItemCount() = files.size

    class FileViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        private val thumbnail = itemView.findViewById<ImageView>(R.id.file_thumbnail)
        private val title = itemView.findViewById<TextView>(R.id.file_title)

        fun bind(file: Movie, onItemClick: (Movie) -> Unit) {
            title.text = file.title
            
            val imageUrl = if (!file.backdropPath.isNullOrEmpty()) {
                file.getBackdropUrl()
            } else {
                file.getPosterUrl()
            }

            Glide.with(itemView.context)
                .load(imageUrl)
                .placeholder(R.drawable.movie_placeholder)
                .error(R.drawable.movie_placeholder)
                .centerCrop()
                .into(thumbnail)

            itemView.setOnClickListener { onItemClick(file) }
        }
    }
}
