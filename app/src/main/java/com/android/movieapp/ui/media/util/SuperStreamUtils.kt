package com.android.movieapp.ui.media.util


internal object SuperStreamUtils {
    // Random 32 length string
    fun randomToken(): String {
        return (0..31).joinToString("") {
            (('0'..'9') + ('a'..'f')).random().toString()
        }
    }

    fun getExpiryDate(): Long {
        // Current time + 12 hours
        return (System.currentTimeMillis() / 1000) + 60 * 60 * 12
    }

}

enum class MediaType(val value: Int, val type: String) {
    Series(2, "series"),
    Movies(1,"single");

    companion object {
        fun getSSMediaType(value: Int?): MediaType {
            return entries.firstOrNull { it.value == value } ?: Movies
        }

    }
}