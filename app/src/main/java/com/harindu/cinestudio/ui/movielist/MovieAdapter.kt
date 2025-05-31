package com.harindu.cinestudio.ui.movielist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.harindu.cinestudio.R // ADDED: Import the R class for resources
import com.harindu.cinestudio.data.model.Movie
import com.harindu.cinestudio.databinding.ItemMovieBinding

class MovieAdapter(private val onClick: (Movie) -> Unit) :
    ListAdapter<Movie, MovieAdapter.MovieViewHolder>(MovieDiffCallback) {

    private val IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500" // w500 is a common size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val binding = ItemMovieBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MovieViewHolder(binding, onClick)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        val movie = getItem(position)
        holder.bind(movie, IMAGE_BASE_URL)
    }

    class MovieViewHolder(
        private val binding: ItemMovieBinding,
        val onClick: (Movie) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(movie: Movie, imageBaseUrl: String) {
            binding.movieTitleTextView.text = movie.title
            binding.movieReleaseDateTextView.text = movie.releaseDate // Assuming this is the correct ID for release year
            // You might also want to bind movie.rating and movie.overview here if your item_movie.xml has them

            // Load movie poster using Glide
            val imageUrl = "$imageBaseUrl${movie.posterPath}"
            Glide.with(binding.moviePosterImageView.context)
                .load(imageUrl)
                .placeholder(R.drawable.movie_placeholder_poster) // FIX: R is now resolved
                .error(R.drawable.error_movie) // FIX: R is now resolved
                .into(binding.moviePosterImageView)

            itemView.setOnClickListener {
                onClick(movie)
            }
        }
    }
}

object MovieDiffCallback : DiffUtil.ItemCallback<Movie>() {
    override fun areItemsTheSame(oldItem: Movie, newItem: Movie): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Movie, newItem: Movie): Boolean {
        return oldItem == newItem
    }
}