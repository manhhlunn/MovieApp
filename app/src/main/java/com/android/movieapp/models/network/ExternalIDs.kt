package com.android.movieapp.models.network


import androidx.annotation.Keep
import com.android.movieapp.ui.detail.SocialData
import com.android.movieapp.ui.detail.SocialType
import com.google.gson.annotations.SerializedName

@Keep
data class ExternalIDs(
    @SerializedName("id")
    val id: Int?,
    @SerializedName("facebook_id")
    val facebookId: String?,
    @SerializedName("imdb_id")
    val imdbId: String?,
    @SerializedName("instagram_id")
    val instagramId: String?,
    @SerializedName("twitter_id")
    val twitterId: String?,
    @SerializedName("wikidata_id")
    val wikidataId: String?,
    @SerializedName("tiktok_id")
    val tiktokId: String?
)

fun ExternalIDs.mapToListSocial(): List<SocialData> {
    return SocialType.entries.mapNotNull {
        SocialData(
            it, when (it) {
                SocialType.Facebook -> facebookId ?: return@mapNotNull null
                SocialType.IMDb -> imdbId ?: return@mapNotNull null
                SocialType.Instagram -> instagramId ?: return@mapNotNull null
                SocialType.Twitter -> twitterId ?: return@mapNotNull null
                SocialType.Wikipedia -> wikidataId ?: return@mapNotNull null
                SocialType.Tiktok -> tiktokId ?: return@mapNotNull null
            }
        )
    }
}