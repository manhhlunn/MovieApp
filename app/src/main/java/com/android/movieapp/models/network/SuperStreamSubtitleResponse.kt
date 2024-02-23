package com.android.movieapp.models.network

import androidx.annotation.Keep
import com.android.movieapp.ui.media.SuperStreamCommon.captionDomains
import com.google.gson.annotations.SerializedName

@Keep
internal data class SuperStreamSubtitleResponse(
    val code: Int? = null,
    val msg: String? = null,
    val data: SubtitleData = SubtitleData()
) {
    @Keep
    data class SuperStreamSubtitleItem(
        @SerializedName("file_path") val filePath: String? = null,
        val lang: String? = null,
        val language: String? = null,
        val order: Int? = null,
    ) {
        companion object {
            fun String.toValidSubtitleFilePath(): String {
                return replace(captionDomains[0], captionDomains[1])
                    .replace(Regex("\\s"), "+")
                    .replace(Regex("[()]")) { result ->
                        "%" + result.value.toCharArray()[0].code.toByte().toString(16)
                    }
            }
        }
    }

    @Keep
    data class SuperStreamSubtitle(
        val language: String? = null,
        val subtitles: List<SuperStreamSubtitleItem> = listOf()
    )

    @Keep
    data class SubtitleData(
        val select: List<String> = listOf(),
        val list: List<SuperStreamSubtitle> = listOf()
    )
}