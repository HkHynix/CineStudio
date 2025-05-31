package com.harindu.cinestudio.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

/**
 * Data class representing a movie genre.
 * This class is Parcelable to allow easy passing between components if needed.
 */
@Parcelize
data class Genre(
    @SerializedName("id")
    val id: Int, // Unique identifier for the genre

    @SerializedName("name")
    val name: String // Name of the genre (e.g., "Action", "Comedy")
) : Parcelable