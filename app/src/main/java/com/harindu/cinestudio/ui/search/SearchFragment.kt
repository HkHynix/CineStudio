package com.harindu.cinestudio.ui.search

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.harindu.cinestudio.data.model.Movie
import com.harindu.cinestudio.databinding.FragmentSearchBinding
import com.harindu.cinestudio.ui.movielist.MovieAdapter
import com.harindu.cinestudio.ui.movielist.MovieListFragmentDirections

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val TAG = "SearchFragment"
    }

    private val viewModel: SearchViewModel by viewModels { SearchViewModel.Factory }

    private lateinit var movieAdapter: MovieAdapter
    private var isLoading = false // Track loading state for pagination

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        Log.d(TAG, "onCreateView: SearchFragment layout inflated.")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: SearchFragment view created. Setting up UI components and observers.")

        setupSearchView()
        setupRecyclerView()
        observeViewModel()

        // Show initial message or restore search query
        if (viewModel.currentSearchQuery.value.isNullOrEmpty()) {
            binding.emptyStateTextViewSearch.text = "Start typing to search for movies!"
            binding.emptyStateTextViewSearch.visibility = View.VISIBLE
        } else {
            // Restore query in SearchView if it exists
            binding.searchView.setQuery(viewModel.currentSearchQuery.value, false)
            // No need to call searchMovies here, as ViewModel will retain results
        }
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    Log.d(TAG, "Search query submitted: $it")
                    viewModel.searchMovies(it, 1) // Start new search, page 1
                    hideKeyboard()
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let {
                    Log.d(TAG, "Search query text changed: $it")
                    // Only trigger search if text is not empty, debounce handled in ViewModel
                    if (it.isNotBlank()) {
                        viewModel.searchMovies(it, 1)
                    } else {
                        // If text is cleared, clear search results immediately
                        viewModel.clearSearchResults()
                        binding.emptyStateTextViewSearch.text = "Start typing to search for movies!"
                        binding.emptyStateTextViewSearch.visibility = View.VISIBLE
                        binding.recyclerViewSearchResults.visibility = View.GONE
                    }
                }
                return true
            }
        })

        binding.searchView.setOnCloseListener {
            Log.d(TAG, "Search view closed. Clearing results.")
            viewModel.clearSearchResults()
            binding.emptyStateTextViewSearch.text = "Start typing to search for movies!"
            binding.emptyStateTextViewSearch.visibility = View.VISIBLE
            true
        }
        Log.d(TAG, "SearchView setup complete.")
    }

    private fun setupRecyclerView() {
        movieAdapter = MovieAdapter { movie ->
            Log.d(TAG, "Search result movie clicked: ${movie.title} (ID: ${movie.id})")
            val action = MovieListFragmentDirections.actionMovieListViewFragmentToMovieDetailFragment(movie)
            findNavController().navigate(action)
        }

        binding.recyclerViewSearchResults.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = movieAdapter
            setHasFixedSize(true)

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                    if (!isLoading && viewModel.isLastPage.value == false) {
                        if ((visibleItemCount + firstVisibleItemPosition) >= (totalItemCount - 5)
                            && firstVisibleItemPosition >= 0
                            && totalItemCount > 0
                        ) {
                            val nextSearchQuery = viewModel.currentSearchQuery.value ?: "" // FIX: Access value
                            if (nextSearchQuery.isNotBlank()) { // Ensure query is not empty before paginating
                                val nextPage = (movieAdapter.itemCount / 20) + 1 // Calculate next page based on currently loaded items
                                Log.d(TAG, "Loading next page of search results: $nextPage for query '$nextSearchQuery'.") // FIX: Access value
                                viewModel.searchMovies(nextSearchQuery, nextPage) // FIX: Pass value
                            }
                        }
                    }
                }
            })
        }
        Log.d(TAG, "RecyclerView setup complete.")
    }

    private fun observeViewModel() {
        viewModel.searchResults.observe(viewLifecycleOwner, Observer { movies ->
            Log.d(TAG, "Search results LiveData observed. Received ${movies?.size ?: 0} movies.")
            movieAdapter.submitList(movies.toList())

            binding.progressBarSearch.visibility = View.GONE

            if (movies.isNullOrEmpty()) {
                // Only show "No movies found" if a search query was actually made and returned nothing
                if (!viewModel.currentSearchQuery.value.isNullOrBlank()) { // FIX: Access value
                    binding.emptyStateTextViewSearch.text = "No movies found for '${viewModel.currentSearchQuery.value}'." // FIX: Access value
                    binding.emptyStateTextViewSearch.visibility = View.VISIBLE
                } else {
                    // If no query, show initial instruction
                    binding.emptyStateTextViewSearch.text = "Start typing to search for movies!"
                    binding.emptyStateTextViewSearch.visibility = View.VISIBLE
                }
                binding.recyclerViewSearchResults.visibility = View.GONE
            } else {
                binding.emptyStateTextViewSearch.visibility = View.GONE
                binding.recyclerViewSearchResults.visibility = View.VISIBLE
            }
        })

        viewModel.isLoading.observe(viewLifecycleOwner, Observer { loading ->
            isLoading = loading
            Log.d(TAG, "isLoading LiveData observed. Is loading: $loading")
            binding.progressBarSearch.visibility = if (loading) View.VISIBLE else View.GONE

            if (loading && movieAdapter.itemCount == 0) {
                binding.recyclerViewSearchResults.visibility = View.GONE
                binding.emptyStateTextViewSearch.visibility = View.GONE
            }
        })

        viewModel.errorMessage.observe(viewLifecycleOwner, Observer { message ->
            if (message != null) {
                Log.e(TAG, "Error message observed: $message")
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                viewModel.clearErrorMessage()

                binding.progressBarSearch.visibility = View.GONE

                if (movieAdapter.itemCount == 0) {
                    binding.emptyStateTextViewSearch.text = "Error: $message\nPlease try again."
                    binding.emptyStateTextViewSearch.visibility = View.VISIBLE
                    binding.recyclerViewSearchResults.visibility = View.GONE
                }
            }
        })

        viewModel.isLastPage.observe(viewLifecycleOwner, Observer { lastPageValue ->
            Log.d(TAG, "isLastPage LiveData observed. Is last page: $lastPageValue")
        })

        // Observe current search query to update SearchView if needed (e.g., after process death)
        viewModel.currentSearchQuery.observe(viewLifecycleOwner, Observer { query ->
            if (binding.searchView.query.toString() != query) {
                binding.searchView.setQuery(query, false)
            }
        })
    }

    private fun hideKeyboard() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView: Binding cleared.")
        _binding = null
    }
}