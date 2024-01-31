package com.android.movieapp.network

object Api {
    const val BASE_URL = "https://api.themoviedb.org/"
    private const val BASE_POSTER_PATH = "https://image.tmdb.org/t/p/w342"
    private const val BASE_BACKDROP_PATH = "https://image.tmdb.org/t/p/w780"
    private const val BASE_ORIGIN_PATH = "https://image.tmdb.org/t/p/original"
    private const val BACK_DROP_PATH =
        "https://img.freepik.com/free-photo/red-light-round-podium-black-background-mock-up_43614-950.jpg"
    const val PAGING_SIZE = 30
    const val API_KEY = "0eaf7cb7af30fc063331797709f03f79"
    const val AI_API_KEY = "AIzaSyAr9-vBl5tzl-97bQ7nHWfO5FbUna0px_o"

    @JvmStatic
    fun getDefaultBackDropPath(): String {
        return BACK_DROP_PATH
    }

    @JvmStatic
    fun getPosterPath(posterPath: String?): String {
        return BASE_POSTER_PATH + posterPath
    }

    @JvmStatic
    fun getOriginalPath(path: String?): String {
        return BASE_ORIGIN_PATH + path
    }

    @JvmStatic
    fun getBackdropPath(backdropPath: String?): String {
        return BASE_BACKDROP_PATH + backdropPath
    }
}
