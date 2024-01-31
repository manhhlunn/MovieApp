package com.android.movieapp.models.network

import androidx.annotation.Keep
import com.android.movieapp.models.entities.Movie
import com.android.movieapp.models.entities.Tv

@Keep
data class PersonMovieResponse(
    val cast: List<Movie>?,
    val crew: List<Movie>?,
    val id: Int?
)

@Keep
data class PersonTvResponse(
    val cast: List<Tv>?,
    val crew: List<Tv>?,
    val id: Int?
)

