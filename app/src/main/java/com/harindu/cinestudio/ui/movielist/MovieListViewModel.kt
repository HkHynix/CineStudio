package com.harindu.cinestudio.ui.movielist

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.harindu.cinestudio.data.model.Movie
import com.harindu.cinestudio.network.RetrofitClient
import com.harindu.cinestudio.data.repository.MovieRepository
import kotlinx.coroutines.launch

class MovieListViewModel(private val repository: MovieRepository) : ViewModel() {

    companion object {
        private const val TAG = "MovieListViewModel"

        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(MovieListViewModel::class.java)) {
                    val repository = MovieRepository(RetrofitClient.tmdbApiService)
                    return MovieListViewModel(repository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }

    // Initialize with an empty mutable list directly
    private val _movies = MutableLiveData<MutableList<Movie>>(mutableListOf())
    val movies: LiveData<MutableList<Movie>> = _movies

    private val _isLoading = MutableLiveData<Boolean>(false) // Initialize to false
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    private val _isLastPage = MutableLiveData<Boolean>(false) // Initialize to false
    val isLastPage: LiveData<Boolean> = _isLastPage

    private val _currentMovieCategory = MutableLiveData<String>("popular") // Default category
    val currentMovieCategory: LiveData<String> = _currentMovieCategory

    init {
        Log.d(TAG, "ViewModel init block called.")
        // Initial fetch only if movies list is empty
        if (_movies.value.isNullOrEmpty()) {
            fetchMoviesByCategory(_currentMovieCategory.value!!, 1)
        }
    }

    /**
     * Fetches movies from the repository based on category and page.
     * @param category The movie category (e.g., "popular", "top_rated", "upcoming", "now_playing").
     * @param page The page number to fetch.
     */
    fun fetchMoviesByCategory(category: String, page: Int) {
        // Prevent re-fetching if already loading or if on the last page and trying to load more
        if (_isLoading.value == true || (_isLastPage.value == true && page > 1)) {
            Log.d(TAG, "Already loading or last page reached, ignoring fetch request for $category page $page.")
            return
        }

        // If category changes, update it and allow the Fragment to clear the list via clearMovies()
        if (_currentMovieCategory.value != category) {
            _currentMovieCategory.postValue(category)
            // The clearMovies() call from the Fragment will handle the list clearing.

        }

        _isLoading.postValue(true)
        _errorMessage.postValue(null) // Clear any previous error
        Log.d(TAG, "Starting movie fetch coroutine for category: $category, page: $page")

        viewModelScope.launch {
            try {
                val response = when (category) {
                    "popular" -> repository.getPopularMovies(page = page)
                    "top_rated" -> repository.getTopRatedMovies(page = page)
                    "upcoming" -> repository.getUpcomingMovies(page = page)
                    "now_playing" -> repository.getNowPlayingMovies(page = page)
                    else -> throw IllegalArgumentException("Unknown movie category: $category")
                }

                Log.d(TAG, "API call completed for $category page $page. Response code: ${response.code()}")

                if (response.isSuccessful) {
                    val newMovies = response.body()?.results ?: emptyList()
                    val currentList = _movies.value ?: mutableListOf() // Get current list or create new

                    if (page == 1) {

                        _movies.postValue(newMovies.toMutableList())
                        Log.d(TAG, "Page 1 data received. Total movies: ${newMovies.size}")
                    } else {
                        // For subsequent pages, append new movies
                        currentList.addAll(newMovies)
                        _movies.postValue(currentList)
                        Log.d(TAG, "Appended ${newMovies.size} movies for page $page. Total movies: ${currentList.size}")
                    }

                    val totalPages = response.body()?.totalPages ?: 1
                    _isLastPage.postValue(page >= totalPages)
                    Log.d(TAG, "Is last page: ${_isLastPage.value}")

                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    val errorMessage = "Failed to load movies: ${response.code()} - $errorBody"
                    _errorMessage.postValue(errorMessage)
                    _isLastPage.postValue(true) // Assume last page on error to prevent endless scrolling attempts
                    Log.e(TAG, "Error: $errorMessage")
                }
            } catch (e: Exception) {
                val errorMessage = "Network or parsing error for $category page $page: ${e.message}"
                _errorMessage.postValue(errorMessage)
                _isLastPage.postValue(true) // Assume last page on error
                Log.e(TAG, "Exception during fetch: $errorMessage", e)
            } finally {
                _isLoading.postValue(false)
                Log.d(TAG, "Movie fetch coroutine for $category page $page finished. isLoading: ${_isLoading.value}")
            }
        }
    }

    /**
     * Clears the current list of movies.
     * This should be called by the Fragment when a new category is selected or on pull-to-refresh.
     */
    fun clearMovies() {
        _movies.postValue(mutableListOf()) // Post an empty list
        _isLastPage.postValue(false) // Reset last page flag
        Log.d(TAG, "Movies list cleared and state reset.")
    }

    fun clearErrorMessage() {
        _errorMessage.postValue(null)
        Log.d(TAG, "Error message cleared.")
    }
}