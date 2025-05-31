package com.harindu.cinestudio.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

/**
 * Data class representing a single movie fetched from the TMDB API.
 * This class is Parcelable to allow easy passing between fragments using Safe Args.
 */
@Parcelize
data class Movie(
    @SerializedName("id")
    val id: Int, // Unique identifier for the movie

    @SerializedName("title")
    val title: String, // Title of the movie

    @SerializedName("poster_path")
    val posterPath: String?, // Path to the movie poster image (can be null)

    @SerializedName("release_date")
    val releaseDate: String?, // Release date of the movie (can be null)

    @SerializedName("vote_average")
    val voteAverage: Double, // Average rating of the movie

    @SerializedName("overview")
    val overview: String?, // A brief overview or synopsis of the movie (can be null)

    @SerializedName("backdrop_path")
    val backdropPath: String?, // Path to the movie backdrop image (can be null, for detail screen)

    @SerializedName("genre_ids")
    val genreIds: List<Int>? // List of genre IDs associated with the movie
) : Parcelable {
    /**
     * Helper function to get the full URL for the movie poster.
     * TMDB image base URL: https://image.tmdb.org/t/p/w500/
     */
    fun getPosterUrl(): String? {
        return posterPath?.let { "https://image.tmdb.org/t/p/w500/$it" }
    }

    /**
     * Helper function to get the full URL for the movie backdrop.
     * TMDB image base URL: https://image.tmdb.org/t/p/w1280/ (or w780 for smaller)
     */
    fun getBackdropUrl(): String? {
        return backdropPath?.let { "https://image.tmdb.org/t/p/w1280/$it" }
    }

    /**
     * Helper function to get the formatted rating string.
     */
    fun getFormattedRating(): String {
        return String.format("%.1f/10", voteAverage)
    }
}