package com.android.movieapp.models.network

import androidx.annotation.Keep
import com.android.movieapp.models.entities.Movie
import com.android.movieapp.models.entities.Person
import com.android.movieapp.models.entities.Tv
import com.google.gson.annotations.SerializedName

@Keep
data class BaseListNetworkResponse<T : Any>(
    @SerializedName("page")
    val page: Int,
    @SerializedName("results")
    val results: List<T>,
    @SerializedName("total_results")
    val totalResults: Int,
    @SerializedName("total_pages")
    val totalPages: Int
)

typealias MovieListResponse = BaseListNetworkResponse<Movie>
typealias TvListResponse = BaseListNetworkResponse<Tv>
typealias PersonListResponse = BaseListNetworkResponse<Person>