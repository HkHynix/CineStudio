package com.harindu.cinestudio.data.repository

import android.util.Log
import com.harindu.cinestudio.data.api.TmdbApiService
import com.harindu.cinestudio.data.model.Movie
import com.harindu.cinestudio.data.model.response.MovieListResponse
import retrofit2.Response

/**
 * Repository for handling movie-related data operations.
 * It abstracts the data sources (e.g., network API, local database).
 */
class MovieRepository(private val apiService: TmdbApiService) {

    companion object {
        private const val TAG = "MovieRepository"
    }

    suspend fun getPopularMovies(page: Int): Response<MovieListResponse> {
        Log.d(TAG, "Fetching popular movies, page: $page")
        return apiService.getPopularMovies(page = page)
    }

    suspend fun getTopRatedMovies(page: Int): Response<MovieListResponse> {
        Log.d(TAG, "Fetching top_rated movies, page: $page")
        return apiService.getTopRatedMovies(page = page)
    }

    suspend fun getUpcomingMovies(page: Int): Response<MovieListResponse> {
        Log.d(TAG, "Fetching upcoming movies, page: $page")
        return apiService.getUpcomingMovies(page = page)
    }

    suspend fun getNowPlayingMovies(page: Int): Response<MovieListResponse> {
        Log.d(TAG, "Fetching now_playing movies, page: $page")
        return apiService.getNowPlayingMovies(page = page)
    }

    suspend fun getMovieDetails(movieId: Int): Response<Movie> {
        Log.d(TAG, "Fetching details for movie ID: $movieId")
        return apiService.getMovieDetails(movieId = movieId)
    }

    suspend fun searchMovies(query: String, page: Int): Response<MovieListResponse> {
        Log.d(TAG, "Searching movies for query: '$query', page: $page")
        return apiService.searchMovies(query = query, page = page)
    }
}