package com.android.movieapp.ui.media

import com.android.movieapp.ui.media.util.CryptographyUtil.base64Decode

internal object SuperStreamCommon {

    val iv = base64Decode("d0VpcGhUbiE=")
    val key = base64Decode("MTIzZDZjZWRmNjI2ZHk1NDIzM2FhMXc2")

    private val baseApiUrl = base64Decode("aHR0cHM6Ly9zaG93Ym94LnNoZWd1Lm5ldA==")
    val apiUrl =
        "$baseApiUrl${base64Decode("L2FwaS9hcGlfY2xpZW50L2luZGV4Lw==")}"

    // Another url because the first one sucks at searching
    // This one was revealed to me in a dream
    val secondApiUrl =
        base64Decode("aHR0cHM6Ly9tYnBhcGkuc2hlZ3UubmV0L2FwaS9hcGlfY2xpZW50L2luZGV4Lw==")

    val thirdApiUrl =
        base64Decode("aHR0cHM6Ly93d3cuZmViYm94LmNvbQ==")

    val fourApiUrl =
        base64Decode("aHR0cHM6Ly93d3cuc2hvd2JveC5tZWRpYQ==")

    const val watchSoMuchAPI = "https://watchsomuch.tv"
    const val openSubAPI = "https://opensubtitles-v3.strem.io"
    const val vidSrcToAPI = "https://vidsrc.to"

    val appKey = base64Decode("bW92aWVib3g=")
    val appId = base64Decode("Y29tLnRkby5zaG93Ym94")
    val appIdSecond = base64Decode("Y29tLm1vdmllYm94cHJvLmFuZHJvaWQ=")
    val captionDomains =
        arrayOf(
            base64Decode("bWJwaW1hZ2VzLmNodWF4aW4uY29t"),
            base64Decode("aW1hZ2VzLnNoZWd1Lm5ldA==")
        )
    const val appVersion = "14.7"
    const val appVersionCode = "160"
}