package com.android.movieapp.ui.configure

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import com.android.movieapp.models.network.CountryItemResponse
import com.android.movieapp.models.network.LanguageItemResponse
import com.android.movieapp.ui.ext.countryIcon
import com.android.movieapp.ui.ext.languageIcon

@Composable
fun LanguageItemView(
    value: LanguageItemResponse,
    modifier: Modifier = Modifier,
    onSelect: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clickable {
                onSelect()
            }) {
        val painter = rememberAsyncImagePainter(
            model = value.iso6391.languageIcon(),
            error = ColorPainter(MaterialTheme.colorScheme.onSecondary)
        )
        Image(
            painter = painter,
            contentDescription = "Language icon",
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .width(40.dp)
                .then(
                    (painter.state as? AsyncImagePainter.State.Success)
                        ?.painter
                        ?.intrinsicSize
                        ?.let { intrinsicSize ->
                            val ratio = intrinsicSize.width / intrinsicSize.height
                            Modifier
                                .aspectRatio(ratio)
                                .shadow(4.dp)
                        } ?: Modifier.aspectRatio(2f)
                ),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = value.englishName + if (value.name.isNullOrEmpty()) "" else " (${value.name})",
            maxLines = 1,
            fontWeight = FontWeight.Bold,
            overflow = TextOverflow.Ellipsis,
            fontSize = 16.sp,
        )
    }
}

@Composable
fun RegionItemView(
    value: CountryItemResponse,
    modifier: Modifier = Modifier,
    onSelect: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clickable {
                onSelect()
            }) {
        val painter = rememberAsyncImagePainter(
            model = value.iso31661.countryIcon(),
            error = ColorPainter(MaterialTheme.colorScheme.onSecondary)
        )
        Image(
            painter = painter,
            contentDescription = "Language icon",
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .width(40.dp)
                .then(
                    (painter.state as? AsyncImagePainter.State.Success)
                        ?.painter
                        ?.intrinsicSize
                        ?.let { intrinsicSize ->
                            val ratio = intrinsicSize.width / intrinsicSize.height
                            Modifier
                                .aspectRatio(ratio)
                                .shadow(4.dp)
                        } ?: Modifier.aspectRatio(2f)
                ),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = value.englishName + if (value.nativeName.isNullOrEmpty()) "" else " (${value.nativeName})",
            maxLines = 1,
            fontWeight = FontWeight.Bold,
            overflow = TextOverflow.Ellipsis,
            fontSize = 16.sp,
        )
    }
}