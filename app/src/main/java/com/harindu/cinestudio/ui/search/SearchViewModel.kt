package com.harindu.cinestudio.ui.search

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.harindu.cinestudio.data.model.Movie
import com.harindu.cinestudio.network.RetrofitClient
import com.harindu.cinestudio.data.repository.MovieRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchViewModel(private val repository: MovieRepository) : ViewModel() {

    companion object {
        private const val TAG = "SearchViewModel"

        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
                    val repository = MovieRepository(RetrofitClient.tmdbApiService)
                    return SearchViewModel(repository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }

    private val _searchResults = MutableLiveData<MutableList<Movie>>(mutableListOf())
    val searchResults: LiveData<MutableList<Movie>> = _searchResults

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    private val _isLastPage = MutableLiveData<Boolean>(false)
    val isLastPage: LiveData<Boolean> = _isLastPage

    // FIX: Expose currentSearchQuery as LiveData
    private val _currentSearchQuery = MutableLiveData<String>("")
    val currentSearchQuery: LiveData<String> = _currentSearchQuery

    private var currentPage: Int = 1 // Internal tracking for pagination
    private var searchJob: Job? = null // To debounce search queries

    /**
     * Initiates a movie search.
     * Uses debouncing to prevent excessive API calls on rapid typing.
     * @param query The search query string.
     * @param page The page number for pagination.
     * @param debounceTimeMillis Time in milliseconds to wait before executing search (for debouncing).
     */
    fun searchMovies(query: String, page: Int = 1, debounceTimeMillis: Long = 300L) {
        // FIX: Use _currentSearchQuery.value
        if (query.isBlank()) {
            _searchResults.value = mutableListOf() // Clear results if query is empty
            _isLastPage.value = false
            _isLoading.value = false
            _errorMessage.value = null
            _currentSearchQuery.value = "" // FIX: Update LiveData
            currentPage = 1
            Log.d(TAG, "Search query is blank. Clearing results.")
            return
        }

        // FIX: Use _currentSearchQuery.value
        if (query == _currentSearchQuery.value && page > 1 && _isLastPage.value == true) {
            Log.d(TAG, "Already on last page for query '$query'. Skipping.")
            return
        }

        // Cancel any previous search job to debounce
        searchJob?.cancel()

        searchJob = viewModelScope.launch {
            if (debounceTimeMillis > 0) {
                delay(debounceTimeMillis) // Debounce the search
            }

            // FIX: Use _currentSearchQuery.value
            if (query != _currentSearchQuery.value || page == 1) {
                // New search query or first page of existing query
                _searchResults.postValue(mutableListOf()) // Clear previous results for new search/first page
                _isLastPage.postValue(false) // Reset last page status
                _currentSearchQuery.postValue(query) // FIX: Update LiveData for query
                currentPage = 1
                Log.d(TAG, "New search initiated for '$query'. Clearing results.")
            } else {
                // Pagination for existing query
                currentPage = page
                Log.d(TAG, "Fetching page $page for query '$query'.")
            }

            _isLoading.postValue(true)
            _errorMessage.postValue(null)

            try {
                val response = repository.searchMovies(query = query, page = currentPage)
                Log.d(TAG, "API call completed for query '$query' page $currentPage. Response code: ${response.code()}")

                if (response.isSuccessful) {
                    val newMovies = response.body()?.results ?: emptyList()
                    val currentList = _searchResults.value ?: mutableListOf()

                    currentList.addAll(newMovies)
                    _searchResults.postValue(currentList)

                    val totalPages = response.body()?.totalPages ?: 1
                    _isLastPage.postValue(currentPage >= totalPages)
                    Log.d(TAG, "Fetched ${newMovies.size} movies for '$query' page $currentPage. Total: ${currentList.size}. Is last page: ${_isLastPage.value}")

                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    val errorMessage = "Failed to search movies: ${response.code()} - $errorBody"
                    _errorMessage.postValue(errorMessage)
                    _isLastPage.postValue(true)
                    Log.e(TAG, "Error: $errorMessage")
                }
            } catch (e: Exception) {
                val errorMessage = "Network or parsing error for search '$query': ${e.message}"
                _errorMessage.postValue(errorMessage)
                _isLastPage.postValue(true)
                Log.e(TAG, "Exception during search: $errorMessage", e)
            } finally {
                _isLoading.postValue(false)
                Log.d(TAG, "Search coroutine for '$query' page $currentPage finished. isLoading: ${_isLoading.value}")
            }
        }
    }

    fun clearSearchResults() {
        _searchResults.value = mutableListOf()
        _currentSearchQuery.value = "" // FIX: Update LiveData
        currentPage = 1
        _isLastPage.value = false
        _isLoading.value = false
        _errorMessage.value = null
        searchJob?.cancel()
        Log.d(TAG, "Search results cleared.")
    }

    fun clearErrorMessage() {
        _errorMessage.postValue(null)
        Log.d(TAG, "Error message cleared.")
    }
}