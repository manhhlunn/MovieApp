package com.android.movieapp.models.network

import androidx.annotation.Keep
import com.android.movieapp.ui.media.util.MediaType.Companion.getSSMediaType
import com.google.gson.annotations.SerializedName


@Keep
data class HomePageValues(
    val code: String? = null,
    val msg: String? = null,
    val data: List<HomePageData> = emptyList()
)

@Keep
data class HomePageData(
    val type: String? = null,
    val name: String? = null,
    @SerializedName("ismore") val isMore: Int? = null,
    val list: List<SuperStreamSearchItem>? = null
)

@Keep
data class SuperStreamSearchResponse(
    val code: String? = null,
    val msg: String? = null,
    val data: SuperStreamSearchResponseDataContent = SuperStreamSearchResponseDataContent()
) {
    @Keep
    data class SuperStreamSearchResponseDataContent(
        @SerializedName("list") val results: List<SuperStreamSearchItem> = listOf(),
        val total: Int = 0,
    )
}

@Keep
data class SuperStreamSearchItem(
    val id: Int? = null,
    val title: String? = null,
    val poster: String? = null,
    @SerializedName("box_type") val boxType: Int? = null,
    @SerializedName("imdb_rating") val imdbRating: String? = null,
    @SerializedName("quality_tag") val qualityTag: String? = null
) {
    companion object {
        fun SuperStreamSearchItem.toSearchResultItem(): SearchResultItem {
            return SearchResultItem(
                id = id.toString(),
                title = title,
                image = poster,
                quality = qualityTag,
                filmType = getSSMediaType(boxType),
                imdbRating = imdbRating
            )
        }

        fun SuperStreamSearchItem.hasContent(): Boolean {
            return id != null && title != null && poster != null && boxType != null
        }
    }
}