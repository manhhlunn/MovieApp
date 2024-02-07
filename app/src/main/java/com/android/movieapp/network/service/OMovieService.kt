package com.android.movieapp.network.service

import com.android.movieapp.MovieApp
import com.android.movieapp.models.network.MyMovieResponse
import com.android.movieapp.models.network.OMovieDetailResponse
import com.android.movieapp.models.network.OMovieResponse
import com.android.movieapp.ui.media.FilterCategory
import com.android.movieapp.ui.media.FilterCountry
import com.android.movieapp.ui.media.MediaType
import com.android.movieapp.ui.media.SortTime

class OMovieRequest(private val oMovieService: OMovieService) {

    suspend fun getMovie(
        type: MediaType = MediaType.PhimBo,
        page: Int,
        time: SortTime,
        filterCategory: FilterCategory?,
        filterCountry: FilterCountry?,
        year: Int?
    ) = oMovieService.request<OMovieResponse>(
        url = "${MovieApp.baseURL}_next/data/s4OlXy8jONoHVWAT5vg7b${type.getFile()}",
        parameters = hashMapOf<String, Any>().apply {
            put("page", page)
            put("sort_field", time.value)
            if (filterCategory != null) put("category", filterCategory.value)
            if (filterCountry != null) put("country", filterCountry.value)
            if (year != null) put("year", year)
        }
    )

    suspend fun getMovieDetail(
        slug: String,
    ) = oMovieService.request<OMovieDetailResponse>(
        url = "https://ophim1.com/phim/$slug"
    )

    suspend fun searchMovie(
        query: String,
        page: Int,
        time: SortTime,
        filterCategory: FilterCategory?,
        filterCountry: FilterCountry?,
        year: Int?
    ) = oMovieService.request<OMovieResponse>(
        url = "${MovieApp.baseURL}_next/data/s4OlXy8jONoHVWAT5vg7b/tim-kiem.json",
        parameters = hashMapOf<String, Any>().apply {
            put("page", page)
            put("sort_field", time.value)
            put("keyword", query)
            if (filterCategory != null) put("category", filterCategory.value)
            if (filterCountry != null) put("country", filterCountry.value)
            if (year != null) put("year", year)
        }
    )

    suspend fun getMyMovies(page: Int) = oMovieService.request<MyMovieResponse>(
        url = "https://manhhlunn.github.io/movie/movie_$page.json"
    )

}