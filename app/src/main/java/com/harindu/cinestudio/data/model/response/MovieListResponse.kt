package com.harindu.cinestudio.data.model.response

import com.google.gson.annotations.SerializedName
import com.harindu.cinestudio.data.model.Movie

/**
 * Data class representing the API response for a list of movies (e.g., popular movies).
 * It contains pagination information and the list of Movie objects.
 */
data class MovieListResponse(
    @SerializedName("page")
    val page: Int, // The current page number of the results

    @SerializedName("results")
    val results: List<Movie>, // The list of Movie objects for the current page

    @SerializedName("total_pages")
    val totalPages: Int, // The total number of pages available

    @SerializedName("total_results")
    val totalResults: Int // The total number of movie results available
)