package com.android.movieapp.models.network

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ExternalSources(
    @SerializedName("m3u8_url") val m3u8Url: String? = null,
    @SerializedName("file") val file: String? = null,
    @SerializedName("label") val label: String? = null,
    @SerializedName("type") val type: String? = null
)