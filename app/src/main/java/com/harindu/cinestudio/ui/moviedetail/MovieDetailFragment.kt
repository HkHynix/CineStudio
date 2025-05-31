package com.harindu.cinestudio.ui.moviedetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels // For Kotlin property delegate for ViewModels
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.harindu.cinestudio.R
import com.harindu.cinestudio.databinding.FragmentMovieDetailsBinding

class MovieDetailFragment : Fragment() {

    private var _binding: FragmentMovieDetailsBinding? = null
    private val binding get() = _binding!!

    // Use navArgs to retrieve arguments passed to this fragment
    private val args: MovieDetailFragmentArgs by navArgs()

    // Initialize the ViewModel using the viewModels KTX extension
    private val viewModel: MovieDetailViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMovieDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Pass the movie received from Safe Args to the ViewModel
        viewModel.setMovie(args.movie)

        // Observe the movie LiveData from the ViewModel
        viewModel.movie.observe(viewLifecycleOwner) { movie ->
            binding.movie = movie

            // Load backdrop image using Glide
            movie.getBackdropUrl()?.let { url ->
                Glide.with(this)
                    .load(url)
                    .placeholder(R.drawable.movie_placeholder_backdrop)
                    .error(R.drawable.movie_placeholder_backdrop)
                    .into(binding.detailMoviePoster)
            } ?: run {
                binding.detailMoviePoster.setImageResource(R.drawable.movie_placeholder_backdrop)
            }

            // Manually set rating text (as data binding with String.format can be tricky directly in XML)
            binding.detailMovieRating.text = movie.getFormattedRating()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}