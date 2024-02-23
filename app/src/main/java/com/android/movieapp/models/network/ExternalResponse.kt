package com.android.movieapp.models.network

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ExternalResponse(
    val data: Data? = null,
) {
    @Keep
    data class Data(
        @SerializedName("file_list") val fileList: ArrayList<FileList>? = arrayListOf(),
    ) {
        @Keep
        data class FileList(
            @SerializedName("fid") val fid: Long? = null,
            @SerializedName("file_name") val fileName: String? = null,
            @SerializedName("oss_fid") val ossFid: Long? = null,
        )
    }
}

@Keep
data class ShareKeyResponse(
    val data: Data? = null
) {
    @Keep
    data class Data(
        val link: String? = null
    )
}