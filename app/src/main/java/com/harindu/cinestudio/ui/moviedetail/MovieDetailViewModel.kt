package com.harindu.cinestudio.ui.moviedetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.harindu.cinestudio.data.model.Movie
// import com.harindu.cinestudio.data.repository.MovieRepository // Uncomment if you add repository dependency later

/**
 * ViewModel for the Movie Detail screen.
 * It holds the details of a single movie.
 */
class MovieDetailViewModel(
    // You can inject a MovieRepository here if you need to fetch
    // additional details or interact with local data (e.g., favorites)
    // private val movieRepository: MovieRepository
) : ViewModel() {

    // MutableLiveData to hold the movie data that the UI will observe
    private val _movie = MutableLiveData<Movie>()
    // Expose as LiveData to the UI, making it immutable from outside
    val movie: LiveData<Movie> get() = _movie

    /**
     * Sets the movie data for the ViewModel. This is typically called from the Fragment
     * after receiving the movie via Safe Args.
     * @param selectedMovie The Movie object to display.
     */
    fun setMovie(selectedMovie: Movie) {
        _movie.value = selectedMovie
    }

    // You can add more functions here, for example:
    // suspend fun fetchMoreDetails(movieId: Int) {
    //     // Use movieRepository to fetch more data like cast, trailers etc.
    // }

    // fun toggleFavorite(movie: Movie) {
    //     // Logic to save/remove movie from local favorites
    // }
}