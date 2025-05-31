package com.harindu.cinestudio.ui.movielist

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.harindu.cinestudio.R
import com.harindu.cinestudio.databinding.FragmentMovieListBinding


class MovieListFragment : Fragment() {

    private var _binding: FragmentMovieListBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val TAG = "MovieListFragment"
    }

    private val viewModel: MovieListViewModel by viewModels { MovieListViewModel.Factory }

    private lateinit var movieAdapter: MovieAdapter

    private var currentPage = 1
    private var isLoading = false // Managed by ViewModel's _isLoading
    private var currentCategory: String = "popular" // Track current category

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMovieListBinding.inflate(inflater, container, false)
        Log.d(TAG, "onCreateView: Fragment layout inflated.")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: Fragment view created. Setting up UI components and observers.")

        setupRecyclerView()
        setupChipFilters()
        setupSwipeRefresh()
        observeViewModel()

        // Initial data fetch. ViewModel's init block already fetches 'popular' for page 1.
        // This check ensures we only trigger a fetch if the ViewModel doesn't have data yet
        if (viewModel.movies.value.isNullOrEmpty()) {
            Log.d(TAG, "Initial fetch triggered in onViewCreated.")
            binding.progressBar.visibility = View.VISIBLE
            // ViewModel's init might already be calling fetchMoviesByCategory,
            // so this might be redundant if init is guaranteed to run first.
            // However, keeping it doesn't hurt, as the ViewModel has logic to prevent duplicate fetches.
            viewModel.fetchMoviesByCategory(currentCategory, 1)
        }
    }

    private fun setupRecyclerView() {
        movieAdapter = MovieAdapter { movie ->
            Log.d(TAG, "Movie clicked: ${movie.title} (ID: ${movie.id})")
            val action = MovieListFragmentDirections.actionMovieListViewFragmentToMovieDetailFragment(movie)
            findNavController().navigate(action)
        }

        binding.recyclerViewMovies.apply {
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
                        // Check if we are near the end of the list (e.g., last 5 items)
                        if ((visibleItemCount + firstVisibleItemPosition) >= (totalItemCount - 5)
                            && firstVisibleItemPosition >= 0
                            && totalItemCount > 0 // Ensure there's at least one item to scroll past
                        ) {
                            currentPage++
                            Log.d(TAG, "Loading next page: $currentPage for category: $currentCategory")
                            viewModel.fetchMoviesByCategory(currentCategory, currentPage)
                        }
                    }
                }
            })
        }
        Log.d(TAG, "RecyclerView setup complete.")
    }

    private fun setupChipFilters() {
        binding.movieFilterChipGroup.apply {
            setOnCheckedStateChangeListener { group, checkedIds ->
                val selectedChipId = checkedIds.firstOrNull()
                val newCategory = when (selectedChipId) {
                    R.id.chipPopular -> "popular"
                    R.id.chipTopRated -> "top_rated"
                    R.id.chipUpcoming -> "upcoming"
                    R.id.chipNowPlaying -> "now_playing"
                    else -> "popular"
                }

                Log.d(TAG, "Chip selected: ${resources.getResourceEntryName(selectedChipId ?: 0)} -> Category: $newCategory")

                if (newCategory != currentCategory) {
                    currentCategory = newCategory
                    currentPage = 1
                    viewModel.clearMovies() // Clear previous list first
                    binding.progressBar.visibility = View.VISIBLE
                    binding.recyclerViewMovies.visibility = View.GONE
                    binding.emptyStateTextView.visibility = View.GONE // Hide empty state during loading
                    viewModel.fetchMoviesByCategory(currentCategory, currentPage) // Then fetch new data
                }
            }
            val initialChipId = when (viewModel.currentMovieCategory.value) {
                "popular" -> R.id.chipPopular
                "top_rated" -> R.id.chipTopRated
                "upcoming" -> R.id.chipUpcoming
                "now_playing" -> R.id.chipNowPlaying
                else -> R.id.chipPopular
            }
            if (checkedChipId != initialChipId) {
                findViewById<com.google.android.material.chip.Chip>(initialChipId)?.isChecked = true
            }
        }
        Log.d(TAG, "Chip filters setup complete.")
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            Log.d(TAG, "SwipeRefresh triggered for category: $currentCategory")
            currentPage = 1
            viewModel.clearMovies() // Clear existing movies for a fresh refresh
            // Show loading indicators
            binding.progressBar.visibility = View.GONE // Progress bar is not needed immediately as swipe refresh shows indicator
            binding.recyclerViewMovies.visibility = View.GONE
            binding.emptyStateTextView.visibility = View.GONE
            viewModel.fetchMoviesByCategory(currentCategory, currentPage)
        }
        Log.d(TAG, "SwipeRefreshLayout setup complete.")
    }

    private fun observeViewModel() {
        viewModel.movies.observe(viewLifecycleOwner, Observer { movies ->
            Log.d(TAG, "movies LiveData observed. Received ${movies?.size ?: 0} movies.")
            movieAdapter.submitList(movies.toList()) // Use .toList() to create a new list for DiffUtil

            binding.swipeRefreshLayout.isRefreshing = false // Stop swipe refresh indicator

            // Decide visibility after data submission
            if (movies.isNullOrEmpty()) {
                binding.emptyStateTextView.visibility = View.VISIBLE
                binding.emptyStateTextView.text = getString(R.string.no_movies_to_display)
                binding.recyclerViewMovies.visibility = View.GONE // Hide RecyclerView if no movies
            } else {
                binding.emptyStateTextView.visibility = View.GONE
                binding.recyclerViewMovies.visibility = View.VISIBLE // Show RecyclerView if movies exist
            }
            binding.progressBar.visibility = View.GONE // Always hide progress bar when data is received
        })

        viewModel.isLoading.observe(viewLifecycleOwner, Observer { loading ->
            isLoading = loading
            Log.d(TAG, "isLoading LiveData observed. Is loading: $loading")

            // Show/hide progress bar for pagination, but not for initial load handled by empty state or swipe refresh
            if (loading && movieAdapter.itemCount > 0) { // Only show progress bar if movies already loaded (i.e., pagination)
                binding.progressBar.visibility = View.VISIBLE
            } else {
                binding.progressBar.visibility = View.GONE
            }
        })

        viewModel.errorMessage.observe(viewLifecycleOwner, Observer { message ->
            if (message != null) {
                Log.e(TAG, "Error message observed: $message")
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                viewModel.clearErrorMessage()

                // Hide loading indicators on error
                binding.progressBar.visibility = View.GONE
                binding.swipeRefreshLayout.isRefreshing = false

                // Show error in empty state if no movies are loaded
                if (movieAdapter.itemCount == 0) {
                    binding.emptyStateTextView.text = "Error: $message\nPlease try again."
                    binding.emptyStateTextView.visibility = View.VISIBLE
                    binding.recyclerViewMovies.visibility = View.GONE // Hide RecyclerView if empty due to error
                } else {
                    binding.emptyStateTextView.visibility = View.GONE // Ensure it's hidden if some movies exist
                }
            }
        })

        viewModel.isLastPage.observe(viewLifecycleOwner, Observer<Boolean> { lastPageValue ->
            Log.d(TAG, "isLastPage LiveData observed. Is last page: $lastPageValue")
        })

        viewModel.currentMovieCategory.observe(viewLifecycleOwner, Observer { category ->
            val chipIdToSelect = when (category) {
                "popular" -> R.id.chipPopular
                "top_rated" -> R.id.chipTopRated
                "upcoming" -> R.id.chipUpcoming
                "now_playing" -> R.id.chipNowPlaying
                else -> R.id.chipPopular
            }
            if (binding.movieFilterChipGroup.checkedChipId != chipIdToSelect) {
                binding.movieFilterChipGroup.check(chipIdToSelect)
            }
            Log.d(TAG, "Current category updated to: $category. Selected chip ID: ${resources.getResourceEntryName(chipIdToSelect)}")
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView: Binding cleared.")
        _binding = null
    }
}