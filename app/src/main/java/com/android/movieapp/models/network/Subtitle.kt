package com.android.movieapp.models.network

import androidx.annotation.Keep
import java.io.Serializable


@Keep
data class Subtitle(
    val url: String,
    val name: String?
)