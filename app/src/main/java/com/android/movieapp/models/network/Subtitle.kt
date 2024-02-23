package com.android.movieapp.models.network

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import java.io.Serializable


@Keep
data class Subtitle(
    val url: String,
    val name: String?
) : Serializable {
    override fun equals(other: Any?): Boolean {
        return try {
            other is String &&
            (url.equals(other, true) ||
            url.contains(other, true))
        } catch (_: Exception) {
            super.equals(other)
        }
    }

    override fun hashCode(): Int {
        var result = url.hashCode()
        result = 31 * result + name.hashCode()
        return result
    }
}