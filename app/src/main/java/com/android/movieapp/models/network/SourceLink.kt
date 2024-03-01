package com.android.movieapp.models.network

import androidx.annotation.Keep
import java.io.Serializable

@Keep
data class SourceLink(
    val name: String,
    val url: String,
)