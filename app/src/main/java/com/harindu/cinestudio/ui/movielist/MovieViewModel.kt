package com.harindu.cinestudio.ui.movielist

import android.util.Log // Using standard Android Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.harindu.cinestudio.data.model.Movie
import com.harindu.cinestudio.data.repository.MovieRepository
import com.harindu.cinestudio.network.RetrofitClient // Assuming RetrofitClient is here
import kotlinx.coroutines.launch

/**
 * ViewModel for the Movie List screen.
 * It fetches and manages movie data, handles pagination, and exposes states to the UI.
 */
class MovieViewModel(private val repository: MovieRepository) : ViewModel() {

    // Define a TAG for Logcat
    companion object {
        private const val TAG = "MovieViewModel"

        // This Factory is crucial for providing the MovieRepository dependency
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(MovieViewModel::class.java)) {
                    // Ensure RetrofitClient.apiService is properly initialized
                    val repository = MovieRepository(RetrofitClient.tmdbApiService)
                    return MovieViewModel(repository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }

    // LiveData to hold the list of movies
    private val _movies = MutableLiveData<MutableList<Movie>>()
    val movies: LiveData<MutableList<Movie>> get() = _movies

    // LiveData to indicate loading state
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    // LiveData to indicate error messages
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    // LiveData to indicate if it's the last page of results
    private val _isLastPage = MutableLiveData<Boolean>()
    val isLastPage: LiveData<Boolean> get() = _isLastPage

    // LiveData to track the currently selected movie category (for filters)
    private val _currentMovieCategory = MutableLiveData<String>()
    val currentMovieCategory: LiveData<String> = _currentMovieCategory

    init {
        // Initialize _movies with an empty list to avoid null pointer exceptions
        _movies.value = mutableListOf()
        Log.d(TAG, "ViewModel init block called.")
        // Set initial category and fetch movies
        _currentMovieCategory.value = "popular" // Default category
        fetchMoviesByCategory(_currentMovieCategory.value!!, 1) // Initial fetch for page 1
    }

    /**
     * Fetches movies from the repository based on category and page.
     * The API key is handled by the TmdbApiService.
     * @param category The movie category (e.g., "popular", "top_rated", "upcoming", "now_playing").
     * @param page The page number to fetch.
     */
    fun fetchMoviesByCategory(category: String, page: Int) {
        // Prevent multiple simultaneous fetches or fetching if it's the last page
        if (_isLoading.value == true || _isLastPage.value == true) {
            Log.d(TAG, "Already loading or last page reached, ignoring fetch request for $category page $page.")
            return
        }

        _isLoading.value = true
        _error.value = null // Clear previous errors
        Log.d(TAG, "Starting movie fetch coroutine for category: $category, page: $page")

        // Clear list for new category if fetching the first page
        if (page == 1) {
            _currentMovieCategory.postValue(category)
            _movies.postValue(mutableListOf())
        }

        viewModelScope.launch {
            try {
                // Call the appropriate repository method based on the category
                // The API key is NOT passed here; it's handled by TmdbApiService
                val response = when (category) {
                    "popular" -> repository.getPopularMovies(page = page) // CORRECTED CALL
                    "top_rated" -> repository.getTopRatedMovies(page = page)
                    "upcoming" -> repository.getUpcomingMovies(page = page)
                    "now_playing" -> repository.getNowPlayingMovies(page = page)
                    else -> throw IllegalArgumentException("Unknown movie category: $category")
                }

                Log.d(TAG, "API call completed for $category page $page. Response code: ${response.code()}")

                if (response.isSuccessful) {
                    val newMovies = response.body()?.results ?: emptyList()
                    val currentList = _movies.value ?: mutableListOf()
                    currentList.addAll(newMovies)
                    _movies.value = currentList // Update LiveData with the new combined list

                    // Check if it's the last page based on total_pages from the API response
                    val totalPages = response.body()?.totalPages ?: 1
                    _isLastPage.value = page >= totalPages
                    Log.d(TAG, "Successfully fetched ${newMovies.size} movies for $category page $page. Total movies: ${currentList.size}. Is last page: ${_isLastPage.value}")

                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    _error.value = "Failed to load movies: ${response.code()} - $errorBody"
                    _isLastPage.value = true // Assume last page on error to stop endless loading attempts
                    Log.e(TAG, "Error: ${_error.value}")
                }
            } catch (e: Exception) {
                _error.value = "Network error: ${e.message}"
                _isLastPage.value = true // Assume last page on error
                Log.e(TAG, "Exception during fetch: ${_error.value}", e)
            } finally {
                _isLoading.value = false
                Log.d(TAG, "Movie fetch coroutine for $category page $page finished.")
            }
        }
    }

    /**
     * Clears any stored error message.
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Clears the current list of movies.
     */
    fun clearMovies() {
        _movies.value = mutableListOf()
        _isLastPage.value = false // Reset last page state when clearing movies
        Log.d(TAG, "Movies list cleared.")
    }
}