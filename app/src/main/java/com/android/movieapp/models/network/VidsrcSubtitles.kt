package com.android.movieapp.models.network

import androidx.annotation.Keep

@Keep
data class VIDSRCSubtitle(
    val label: String? = null,
    val file: String? = null,
)

@Keep
data class OsSubtitles(
    val url: String? = null,
    val lang: String? = null,
)

@Keep
data class OsResult(
    val subtitles: ArrayList<OsSubtitles>? = null,
)

@Keep
data class WatchSoMuchSubtitle(
    val url: String? = null,
   val label: String? = null,
)

@Keep
data class WatchSoMuchSubResponses(
    val subtitles: ArrayList<WatchSoMuchSubtitle>? = null,
)

@Keep
data class WatchSoMuchTorrents(
    val id: Int? = null,
    val movieId: Int? = null,
    val season: Int? = null,
    val episode: Int? = null,
)

@Keep
data class WatchSoMuchMovie(
    val torrents: ArrayList<WatchSoMuchTorrents>? = null,
)

@Keep
data class WatchSoMuchResponses(
    val movie: WatchSoMuchMovie? = null,
)