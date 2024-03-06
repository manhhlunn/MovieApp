package com.android.movieapp.ui.home.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val LocalDarkTheme = compositionLocalOf { mutableStateOf(false) }

@Composable
fun TopAppBar(onSettingsClicked: () -> Unit) {

    val isDarkTheme = LocalDarkTheme.current
    val iconTint = MaterialTheme.colorScheme.primary
    Row(
        Modifier
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
            )
            .background(MaterialTheme.colorScheme.onPrimary)
            .statusBarsPadding()
            .padding(start = 4.dp, bottom = 4.dp, end = 4.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        IconButton(onClick = onSettingsClicked) {
            Icon(
                Icons.Default.Settings,
                contentDescription = "Settings",
                tint = iconTint,
            )
        }

        val icon = if (isDarkTheme.value) NightsStay else WbSunny
        IconButton(onClick = { isDarkTheme.value = !isDarkTheme.value }) {
            Icon(icon, contentDescription = "Mode", tint = iconTint)
        }
    }
}

@Composable
fun TopAppBarDetail(name: String?, onBackClicked: () -> Unit) {
    Row(
        Modifier
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
            )
            .background(MaterialTheme.colorScheme.onPrimary)
            .statusBarsPadding()
            .padding(start = 4.dp, bottom = 4.dp, end = 4.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClicked) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.primary,
            )
        }
        Text(
            text = name ?: "",
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                shadow = Shadow(
                    color = MaterialTheme.colorScheme.secondary,
                    offset = Offset(0f, 0f),
                    blurRadius = 0.5f
                )
            ),
        )
    }
}

val NightsStay: ImageVector
    get() {
        if (nightsStay != null) {
            return nightsStay!!
        }
        nightsStay = materialIcon(name = "Filled.NightsStay") {
            materialPath {
                moveTo(11.1f, 12.08f)
                curveTo(8.77f, 7.57f, 10.6f, 3.6f, 11.63f, 2.01f)
                curveTo(6.27f, 2.2f, 1.98f, 6.59f, 1.98f, 12.0f)
                curveToRelative(0.0f, 0.14f, 0.02f, 0.28f, 0.02f, 0.42f)
                curveTo(2.62f, 12.15f, 3.29f, 12.0f, 4.0f, 12.0f)
                curveToRelative(1.66f, 0.0f, 3.18f, 0.83f, 4.1f, 2.15f)
                curveTo(9.77f, 14.63f, 11.0f, 16.17f, 11.0f, 18.0f)
                curveToRelative(0.0f, 1.52f, -0.87f, 2.83f, -2.12f, 3.51f)
                curveToRelative(0.98f, 0.32f, 2.03f, 0.5f, 3.11f, 0.5f)
                curveToRelative(3.5f, 0.0f, 6.58f, -1.8f, 8.37f, -4.52f)
                curveTo(18.0f, 17.72f, 13.38f, 16.52f, 11.1f, 12.08f)
                close()
            }
            materialPath {
                moveTo(7.0f, 16.0f)
                lineToRelative(-0.18f, 0.0f)
                curveTo(6.4f, 14.84f, 5.3f, 14.0f, 4.0f, 14.0f)
                curveToRelative(-1.66f, 0.0f, -3.0f, 1.34f, -3.0f, 3.0f)
                reflectiveCurveToRelative(1.34f, 3.0f, 3.0f, 3.0f)
                curveToRelative(0.62f, 0.0f, 2.49f, 0.0f, 3.0f, 0.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                curveTo(9.0f, 16.9f, 8.1f, 16.0f, 7.0f, 16.0f)
                close()
            }
        }
        return nightsStay!!
    }

private var nightsStay: ImageVector? = null

val WbSunny: ImageVector
    get() {
        if (wbSunny != null) {
            return wbSunny!!
        }
        wbSunny = materialIcon(name = "Filled.WbSunny") {
            materialPath {
                moveTo(6.76f, 4.84f)
                lineToRelative(-1.8f, -1.79f)
                lineToRelative(-1.41f, 1.41f)
                lineToRelative(1.79f, 1.79f)
                lineToRelative(1.42f, -1.41f)
                close()
                moveTo(4.0f, 10.5f)
                lineTo(1.0f, 10.5f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(3.0f)
                verticalLineToRelative(-2.0f)
                close()
                moveTo(13.0f, 0.55f)
                horizontalLineToRelative(-2.0f)
                lineTo(11.0f, 3.5f)
                horizontalLineToRelative(2.0f)
                lineTo(13.0f, 0.55f)
                close()
                moveTo(20.45f, 4.46f)
                lineToRelative(-1.41f, -1.41f)
                lineToRelative(-1.79f, 1.79f)
                lineToRelative(1.41f, 1.41f)
                lineToRelative(1.79f, -1.79f)
                close()
                moveTo(17.24f, 18.16f)
                lineToRelative(1.79f, 1.8f)
                lineToRelative(1.41f, -1.41f)
                lineToRelative(-1.8f, -1.79f)
                lineToRelative(-1.4f, 1.4f)
                close()
                moveTo(20.0f, 10.5f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(3.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(-3.0f)
                close()
                moveTo(12.0f, 5.5f)
                curveToRelative(-3.31f, 0.0f, -6.0f, 2.69f, -6.0f, 6.0f)
                reflectiveCurveToRelative(2.69f, 6.0f, 6.0f, 6.0f)
                reflectiveCurveToRelative(6.0f, -2.69f, 6.0f, -6.0f)
                reflectiveCurveToRelative(-2.69f, -6.0f, -6.0f, -6.0f)
                close()
                moveTo(11.0f, 22.45f)
                horizontalLineToRelative(2.0f)
                lineTo(13.0f, 19.5f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(2.95f)
                close()
                moveTo(3.55f, 18.54f)
                lineToRelative(1.41f, 1.41f)
                lineToRelative(1.79f, -1.8f)
                lineToRelative(-1.41f, -1.41f)
                lineToRelative(-1.79f, 1.8f)
                close()
            }
        }
        return wbSunny!!
    }

private var wbSunny: ImageVector? = null