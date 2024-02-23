package com.android.movieapp.network.service

import com.android.movieapp.MovieApp
import com.android.movieapp.models.network.CustomException
import com.android.movieapp.models.network.EpisodeResponse
import com.android.movieapp.models.network.ExternalResponse
import com.android.movieapp.models.network.ExternalSources
import com.android.movieapp.models.network.HomePageValues
import com.android.movieapp.models.network.NetworkResponse
import com.android.movieapp.models.network.OMovieDetailResponse
import com.android.movieapp.models.network.OMovieResponse
import com.android.movieapp.models.network.OsResult
import com.android.movieapp.models.network.ShareKeyResponse
import com.android.movieapp.models.network.SourceLink
import com.android.movieapp.models.network.Subtitle
import com.android.movieapp.models.network.SuperStreamDownloadResponse
import com.android.movieapp.models.network.SuperStreamResponse
import com.android.movieapp.models.network.SuperStreamSearchResponse
import com.android.movieapp.models.network.SuperStreamSubtitleResponse
import com.android.movieapp.models.network.SuperStreamSubtitleResponse.SuperStreamSubtitleItem.Companion.toValidSubtitleFilePath
import com.android.movieapp.models.network.VIDSRCSubtitle
import com.android.movieapp.models.network.WatchSoMuchResponses
import com.android.movieapp.models.network.WatchSoMuchSubResponses
import com.android.movieapp.ui.ext.asyncMapIndexed
import com.android.movieapp.ui.ext.mapAsync
import com.android.movieapp.ui.media.FilterCategory
import com.android.movieapp.ui.media.FilterCountry
import com.android.movieapp.ui.media.MediaType
import com.android.movieapp.ui.media.SuperStreamCommon
import com.android.movieapp.ui.media.SuperStreamCommon.appId
import com.android.movieapp.ui.media.SuperStreamCommon.appIdSecond
import com.android.movieapp.ui.media.SuperStreamCommon.appVersion
import com.android.movieapp.ui.media.SuperStreamCommon.fourApiUrl
import com.android.movieapp.ui.media.SuperStreamCommon.openSubAPI
import com.android.movieapp.ui.media.SuperStreamCommon.thirdApiUrl
import com.android.movieapp.ui.media.SuperStreamCommon.vidSrcToAPI
import com.android.movieapp.ui.media.SuperStreamCommon.watchSoMuchAPI
import com.android.movieapp.ui.media.util.CipherUtils
import com.android.movieapp.ui.media.util.CryptographyUtil
import com.android.movieapp.ui.media.util.MD5Utils
import com.android.movieapp.ui.media.util.SSMediaType
import com.android.movieapp.ui.media.util.SubtitleHelper
import com.android.movieapp.ui.media.util.SuperStreamUtils
import com.android.movieapp.ui.media.util.SuperStreamUtils.getExpiryDate
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import okhttp3.ResponseBody
import okhttp3.internal.closeQuietly
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import retrofit2.HttpException
import retrofit2.Response

class MediaRequest(private val mediaService: MediaService) {

    suspend fun getOMovie(
        type: MediaType = MediaType.PhimBo,
        page: Int,
        filterCategory: FilterCategory?,
        filterCountry: FilterCountry?,
        year: Int?
    ) = mediaService.request<OMovieResponse>(
        url = "${MovieApp.baseURL}_next/data/s4OlXy8jONoHVWAT5vg7b${type.getFile()}",
        parameters = hashMapOf<String, Any>().apply {
            put("page", page)
            if (filterCategory != null) put("category", filterCategory.value)
            if (filterCountry != null) put("country", filterCountry.value)
            if (year != null) put("year", year)
        }
    )

    suspend fun getOMovieDetail(
        slug: String,
    ) = mediaService.request<OMovieDetailResponse>(
        url = "https://ophim1.com/phim/$slug"
    )

    suspend fun searchOMovie(
        query: String,
        page: Int,
        filterCategory: FilterCategory?,
        filterCountry: FilterCountry?,
        year: Int?
    ) = mediaService.request<OMovieResponse>(
        url = "${MovieApp.baseURL}_next/data/s4OlXy8jONoHVWAT5vg7b/tim-kiem.json",
        parameters = hashMapOf<String, Any>().apply {
            put("page", page)
            put("keyword", query)
            if (filterCategory != null) put("category", filterCategory.value)
            if (filterCountry != null) put("country", filterCountry.value)
            if (year != null) put("year", year)
        }
    )

    private val headers = mapOf(
        "Platform" to "android",
        "Accept" to "charset=utf-8",
    )

    private suspend inline fun <reified T : Any> superStreamRequestCall(
        query: String,
        useAlternativeApi: Boolean = false,
    ): NetworkResponse<T> {
        try {
            val encryptedQuery =
                CipherUtils.encrypt(query, SuperStreamCommon.key, SuperStreamCommon.iv)!!
            val appKeyHash = MD5Utils.md5(SuperStreamCommon.appKey)!!
            val verify =
                CipherUtils.getVerify(
                    encryptedQuery,
                    SuperStreamCommon.appKey,
                    SuperStreamCommon.key
                )
            val newBody =
                """{"app_key":"$appKeyHash","verify":"$verify","encrypt_data":"$encryptedQuery"}"""

            val data = mapOf(
                "data" to CryptographyUtil.base64Encode(newBody.toByteArray()),
                "appid" to "27",
                "platform" to "android",
                "version" to SuperStreamCommon.appVersionCode,
                "medium" to "Website",
                "token" to SuperStreamUtils.randomToken()
            )

            val url =
                if (useAlternativeApi) SuperStreamCommon.secondApiUrl else SuperStreamCommon.apiUrl

            val response = mediaService.post(
                url = url,
                parameters = data,
                headers = headers
            )

            if (response.isSuccessful) {
                val responseBody = response.body()?.string()
                if (responseBody?.contains(
                        other = """"msg":"success""",
                        ignoreCase = true
                    ) == true
                ) {
                    val t = fromJson<T>(responseBody)
                    if (t != null) return NetworkResponse.Success(t)
                }
            }
            return NetworkResponse.Error(CustomException.RequestFail(HttpException(response)))
        } catch (e: HttpException) {
            return NetworkResponse.Error(CustomException.RequestFail(e))
        } catch (e: Exception) {
            return NetworkResponse.Error(CustomException.Normal(e))
        }
    }

    suspend fun getSuperStream(
        page: Int,
        itemsPerPage: Int
    ): NetworkResponse<HomePageValues> {
        val hideNsfw = 0
        return superStreamRequestCall<HomePageValues>(
            """{"childmode":"$hideNsfw","app_version":"$appVersion","appid":"$appIdSecond","module":"Home_list_type_v5","channel":"Website","page":"$page","lang":"en","type":"all","pagelimit":"$itemsPerPage","expired_date":"${getExpiryDate()}","platform":"android"}
            """.trimIndent(),
            true
        )
    }

    suspend fun searchSuperStream(
        query: String,
        page: Int,
        itemsPerPage: Int
    ): NetworkResponse<SuperStreamSearchResponse> {
        val apiQuery =
            """{"childmode":"0","app_version":"$appVersion","appid":"$appIdSecond","module":"Search4","channel":"Website","page":"$page","lang":"en","type":"all","keyword":"$query","pagelimit":"$itemsPerPage","expired_date":"${getExpiryDate()}","platform":"android"}"""

        return superStreamRequestCall<SuperStreamSearchResponse>(apiQuery, true)
    }

    suspend fun getSuperStreamMovieDetail(
        filmId: String
    ): NetworkResponse<SuperStreamResponse.SuperStreamMovieDetail> {
        val apiQuery =
            """{"childmode":"0","uid":"","app_version":"$appVersion","appid":"$appIdSecond","module":"Movie_detail","channel":"Website","mid":"$filmId","lang":"en","expired_date":"${getExpiryDate()}","platform":"android","oss":"","group":""}"""

        return superStreamRequestCall<SuperStreamResponse.SuperStreamMovieDetail>(apiQuery)
    }

    suspend fun getSuperStreamTvShowDetail(
        tvId: String
    ): NetworkResponse<SuperStreamResponse.SuperStreamTvDetail> {
        val apiQuery =
            """{"childmode":"0","uid":"","app_version":"$appVersion","appid":"$appIdSecond","module":"TV_detail_1","display_all":"1","channel":"Website","lang":"en","expired_date":"${getExpiryDate()}","platform":"android","tid":"$tvId"}"""



        when (val res = superStreamRequestCall<SuperStreamResponse.SuperStreamTvDetail>(apiQuery)) {
            is NetworkResponse.Success -> {
                val episodes = res.data.data?.season?.mapNotNull {
                    val seasonQuery =
                        """{"childmode":"0","app_version":"$appVersion","year":"0","appid":"$appIdSecond","module":"TV_episode","display_all":"1","channel":"Website","season":"$it","lang":"en","expired_date":"${getExpiryDate()}","platform":"android","tid":"$tvId"}"""
                    val episode = superStreamRequestCall<EpisodeResponse>(seasonQuery)
                    return@mapNotNull if (episode is NetworkResponse.Success) {
                        episode.data.data
                    } else null
                }?.flatten()

                return NetworkResponse.Success(res.data.copy(data = res.data.data?.copy(episode = episodes)))
            }

            is NetworkResponse.Error -> {
                return res
            }
        }
    }

    suspend fun getSourceLinksSuperStream(
        id: Int,
        season: Int?,
        episode: Int?,
        mediaId: Int?,
        imdbId: String?
    ) = coroutineScope {
        val srcs = mutableListOf<SourceLink>()
        val subtitles = mutableListOf<Subtitle>()
        val deferred = mutableListOf<Deferred<Any>>()
        deferred.add(async {
            val (src, sub) = invokeInternalSource(id, season, episode)
            srcs.addAll(src)
            subtitles.addAll(sub)
        })
        deferred.add(async { srcs.addAll(invokeExternalSource(mediaId, season, episode)) })
        deferred.add(async { subtitles.addAll(invokeWatchSoMuch(imdbId, season, episode)) })
        deferred.add(async { subtitles.addAll(invokeOpenSub(imdbId, season, episode)) })
        deferred.add(async { subtitles.addAll(invokeVIDSRCTO(imdbId, season, episode)) })
        deferred.awaitAll()
        return@coroutineScope Pair(srcs, subtitles)
    }

    private suspend fun invokeInternalSource(
        filmId: Int? = null,
        season: Int? = null,
        episode: Int? = null
    ): Pair<List<SourceLink>, List<Subtitle>> {
        val isMovie = season == null && episode == null

        val query = if (isMovie) {
            """{"childmode":"0","uid":"","app_version":"$appVersion","appid":"$appId","module":"Movie_downloadurl_v3","channel":"Website","mid":"$filmId","lang":"en","expired_date":"${getExpiryDate()}","platform":"android","oss":"1","group":""}"""
        } else {
            """{"childmode":"0","app_version":"$appVersion","module":"TV_downloadurl_v3","channel":"Website","episode":"$episode","expired_date":"${getExpiryDate()}","platform":"android","tid":"$filmId","oss":"1","uid":"","appid":"$appId","season":"$season","lang":"en","group":""}"""
        }

        val downloadResponse = superStreamRequestCall<SuperStreamDownloadResponse>(query, false)

        if (downloadResponse is NetworkResponse.Success) {
            val data = downloadResponse.data.data.list.find {
                it.path.isNullOrBlank().not()
            } ?: return emptyList<SourceLink>() to emptyList()

            val srcResponses = downloadResponse.data.data.list.mapNotNull {
                return@mapNotNull if (
                    !it.path.isNullOrBlank()
                    && !it.realQuality.isNullOrBlank()
                ) {
                    SourceLink(
                        name = "${it.realQuality} server",
                        url = it.path
                    )
                } else null
            }

            val subtitleQuery = if (isMovie) {
                """{"childmode":"0","fid":"${data.fid}","uid":"","app_version":"$appVersion","appid":"$appId","module":"Movie_srt_list_v2","channel":"Website","mid":"$filmId","lang":"en","expired_date":"${getExpiryDate()}","platform":"android"}"""
            } else {
                """{"childmode":"0","fid":"${data.fid}","app_version":"$appVersion","module":"TV_srt_list_v2","channel":"Website","episode":"$episode","expired_date":"${getExpiryDate()}","platform":"android","tid":"$filmId","uid":"","appid":"$appId","season":"$season","lang":"en"}"""
            }

            val subtitlesResponse =
                superStreamRequestCall<SuperStreamSubtitleResponse>(subtitleQuery)

            if (subtitlesResponse is NetworkResponse.Success) {
                val srcSubtitles = subtitlesResponse.data.data.list.mapAsync { subtitle ->
                    subtitle.subtitles
                        .sortedWith(compareByDescending { it.order })
                        .mapNotNull {
                            if (
                                it.filePath != null
                                && it.lang != null
                            ) {
                                Subtitle(
                                    url = it.filePath.toValidSubtitleFilePath(),
                                    name = "${it.language ?: "UNKNOWN"} - Votes: ${it.order}",
                                )
                            } else null
                        }
                }.flatten()

                return srcResponses to srcSubtitles
            } else {
                return srcResponses to emptyList()
            }
        }

        return emptyList<SourceLink>() to emptyList()
    }

    private suspend fun invokeExternalSource(
        mediaId: Int? = null,
        season: Int? = null,
        episode: Int? = null
    ): List<SourceLink> {
        val (seasonSlug, episodeSlug) = getEpisodeSlug(season, episode)
        val type =
            if (season == null && episode == null) SSMediaType.Movies.value else SSMediaType.Series.value
        val shareKey =
            mediaService.getResponse("$fourApiUrl/index/share_link?id=${mediaId}&type=$type")
                .toObject<ShareKeyResponse>()?.data?.link?.substringAfterLast("/")
                ?: return emptyList()

        val shareRes =
            mediaService.getResponse("$thirdApiUrl/file/file_share_list?share_key=$shareKey")
                .toObject<ExternalResponse>()?.data ?: return emptyList()

        val fids = if (season == null) {
            shareRes.fileList
        } else {
            val parentId =
                shareRes.fileList?.find { it.fileName.equals("season $season", true) }?.fid
            mediaService.getResponse(
                "$thirdApiUrl/file/file_share_list?share_key=$shareKey&parent_id=$parentId&page=1"
            ).toObject<ExternalResponse>()?.data?.fileList?.filter {
                it.fileName?.contains("s${seasonSlug}e${episodeSlug}", true) == true
            }
        } ?: return emptyList()

        val srcLinks = mutableListOf<SourceLink>()

        fids.asyncMapIndexed { index, fileList ->
            val player =
                mediaService.getResponse("$thirdApiUrl/file/player?fid=${fileList.fid}&share_key=$shareKey")
                    .body()?.text() ?: return@asyncMapIndexed
            val sources = "sources\\s*=\\s*(.*);".toRegex().find(player)?.groupValues?.get(1)
            val qualities = "quality_list\\s*=\\s*(.*);".toRegex().find(player)?.groupValues?.get(1)
            listOf(sources, qualities).forEach {
                fromJson<List<ExternalSources>>(it ?: return@forEach)?.forEach org@{ source ->
                    if (!(source.label == "AUTO" || source.type == "video/mp4")) return@org
                    srcLinks.add(
                        SourceLink(
                            "External [Server ${index + 1}]",
                            (source.m3u8Url ?: source.file)?.replace("\\/", "/") ?: return@org,
                        )
                    )
                }
            }
        }

        return srcLinks
    }

    private suspend fun invokeWatchSoMuch(
        imdbId: String? = null,
        season: Int? = null,
        episode: Int? = null
    ): List<Subtitle> {
        val id = imdbId?.removePrefix("tt")
        val epsId = mediaService.post(
            "$watchSoMuchAPI/Watch/ajMovieTorrents.aspx",
            parameters = mapOf(
                "index" to "0",
                "mid" to "$id",
                "wsk" to "30fb68aa-1c71-4b8c-b5d4-4ca9222cfb45",
                "lid" to "",
                "liu" to ""
            ), headers = mapOf("X-Requested-With" to "XMLHttpRequest")
        ).toObject<WatchSoMuchResponses>()?.movie?.torrents?.let { eps ->
            if (season == null) {
                eps.firstOrNull()?.id
            } else {
                eps.find { it.episode == episode && it.season == season }?.id
            }
        } ?: return emptyList()

        val (seasonSlug, episodeSlug) = getEpisodeSlug(
            season,
            episode
        )

        val subUrl = if (season == null) {
            "$watchSoMuchAPI/Watch/ajMovieSubtitles.aspx?mid=$id&tid=$epsId&part="
        } else {
            "$watchSoMuchAPI/Watch/ajMovieSubtitles.aspx?mid=$id&tid=$epsId&part=S${seasonSlug}E${episodeSlug}"
        }

        return mediaService.getResponse(subUrl)
            .toObject<WatchSoMuchSubResponses>()?.subtitles
            ?.mapNotNull { sub ->
                Subtitle(
                    fixUrl(sub.url ?: return@mapNotNull null, watchSoMuchAPI),
                    "${sub.label} [WatchSoMuch]"
                )
            } ?: emptyList()
    }

    private suspend fun invokeOpenSub(
        imdbId: String? = null,
        season: Int? = null,
        episode: Int? = null
    ): List<Subtitle> {
        val slug = if (season == null) {
            "movie/$imdbId"
        } else {
            "series/$imdbId:$season:$episode"
        }
        return mediaService.getResponse("${openSubAPI}/subtitles/$slug.json")
            .toObject<OsResult>()
            ?.subtitles
            ?.mapNotNull { sub ->
                Subtitle(
                    sub.url ?: return@mapNotNull null,
                    "${SubtitleHelper.fromThreeLettersToLanguage(sub.lang ?: "") ?: sub.lang} [OpenSub]"
                )

            } ?: emptyList()
    }

    private suspend fun invokeVIDSRCTO(
        imdbId: String?,
        season: Int?,
        episode: Int?
    ): List<Subtitle> {
        val url = if (season == null) {
            "$vidSrcToAPI/embed/movie/$imdbId"
        } else {
            "$vidSrcToAPI/embed/tv/$imdbId/$season/$episode"
        }

        val mediaId = mediaService.getResponse(url).body()?.text()?.document()
            ?.selectFirst("ul.episodes li a")?.attr("data-id") ?: return emptyList()

        return mediaService.getResponse("$vidSrcToAPI/ajax/embed/episode/$mediaId/subtitles")
            .toObject<List<VIDSRCSubtitle>>()?.mapNotNull {
                Subtitle(
                    it.file ?: return@mapNotNull null,
                    "${it.label} [VID_SRC]"
                )
            } ?: emptyList()
    }

    private inline fun <reified T> Response<ResponseBody>.toObject(): T? {
        return if (isSuccessful) {
            val json = body()?.string() ?: return null
            fromJson<T>(json)
        } else null
    }

    private fun ResponseBody.text(): String {
        return string().also {
            closeQuietly()
        }
    }

    private fun String.document(): Document {
        return Jsoup.parse(this)
    }

    private fun fixUrl(url: String, domain: String): String {
        if (url.startsWith("http")) {
            return url
        }
        if (url.isEmpty()) {
            return ""
        }

        val startsWithNoHttp = url.startsWith("//")
        if (startsWithNoHttp) {
            return "https:$url"
        } else {
            if (url.startsWith('/')) {
                return domain + url
            }
            return "$domain/$url"
        }
    }

    private fun getEpisodeSlug(
        season: Int? = null,
        episode: Int? = null,
    ): Pair<String, String> {
        return if (season == null || episode == null) {
            "" to ""
        } else {
            (if (season < 10) "0$season" else "$season") to (if (episode < 10) "0$episode" else "$episode")
        }
    }
}