package com.android.movieapp.models.network

import android.os.Parcelable
import androidx.annotation.Keep
import com.android.movieapp.ui.media.util.MediaType
import kotlinx.parcelize.Parcelize


@Keep
@Parcelize
data class SearchResultItem(
    val id: String? = null,
    val title: String? = null,
    val image: String? = null,
    val filmType: MediaType? = null,
    val quality: String? = null,
    val imdbRating: String? = null
) : Parcelable
