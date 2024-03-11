package com.android.movieapp.ui.home.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.movieapp.R
import com.android.movieapp.models.entities.MediaHistory
import com.android.movieapp.models.entities.Person
import com.android.movieapp.models.network.GenreItemResponse
import com.android.movieapp.network.Api
import com.android.movieapp.network.service.SortValue
import com.android.movieapp.ui.ext.CircleGlowingImage
import com.android.movieapp.ui.ext.DropdownItem
import com.android.movieapp.ui.ext.FilterRow
import com.android.movieapp.ui.ext.ProgressiveGlowingImage
import com.android.movieapp.ui.ext.makeTimeString
import com.android.movieapp.ui.home.Includes
import com.android.movieapp.ui.home.IncludesData
import com.android.movieapp.ui.theme.AppYellow

@Composable
fun DropDownLine(
    resId: Int,
    dropdownItems: List<DropdownItem>,
    dropdownItem: DropdownItem,
    onSelected: (DropdownItem) -> Unit
) {
    if (dropdownItems.isNotEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 8.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            FilterRow(
                resId,
                dropdownItems,
                dropdownItem,
                onSelected = {
                    onSelected(it)
                },
            )
        }
    }
}

@Composable
fun SortByLine(
    current: SortValue,
    onSelected: (SortValue) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 8.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "${stringResource(id = R.string.sort_by)} : ",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                SortValue.entries.forEachIndexed { index, it ->
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = it.display,
                        color = if (it == current) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            shadow = Shadow(
                                color = Color.Black,
                                offset = Offset(0f, 0f),
                                blurRadius = 0.5f
                            )
                        ),
                        modifier = Modifier
                            .background(
                                if (it == current) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSecondary,
                                RoundedCornerShape(50)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .clickable {
                                onSelected.invoke(it)
                            },
                    )
                    if (index == SortValue.entries.size - 1) {
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun IncludeLine(
    current: List<IncludesData>,
    onSelected: (List<IncludesData>) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 8.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "${stringResource(id = R.string.include)} : ",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                current.forEachIndexed { index, it ->
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (it.type) {
                            Includes.FAVORITE -> stringResource(id = R.string.fav_people)
                            Includes.WATCHED -> stringResource(id = R.string.watched_data)
                            Includes.ENDED -> stringResource(id = R.string.ended_series)
                        },
                        color = if (it.value) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            shadow = Shadow(
                                color = Color.Black,
                                offset = Offset(0f, 0f),
                                blurRadius = 0.5f
                            )
                        ),
                        modifier = Modifier
                            .background(
                                if (it.value) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSecondary,
                                RoundedCornerShape(50)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .clickable {
                                val new = current.toMutableList()
                                new[index] = it.copy(value = !it.value)
                                onSelected.invoke(new)
                            },
                    )
                    if (index == current.size - 1) {
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun YearLine(
    current: List<Int>,
    values: List<Int>,
    onSelected: (List<Int>) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 8.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "${stringResource(id = R.string.year)} : ",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                values.forEachIndexed { index, it ->
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (it == -1) "None" else it.toString(),
                        color = if (if (it == -1 && current.isEmpty()) true else (it in current)) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            shadow = Shadow(
                                color = Color.Black,
                                offset = Offset(0f, 0f),
                                blurRadius = 0.5f
                            )
                        ),
                        modifier = Modifier
                            .background(
                                if (if (it == -1 && current.isEmpty()) true else (it in current)) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSecondary,
                                RoundedCornerShape(50)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .clickable {
                                val selected = when {
                                    (it == -1) -> emptyList()
                                    current.isEmpty() -> listOf(it)
                                    else -> {
                                        val min = current.min()
                                        val max = current.max()
                                        when {
                                            (it > max) -> min..it
                                            (it < min) -> it..max
                                            else -> it..max
                                        }.toList()
                                    }
                                }
                                onSelected.invoke(selected)
                            },
                    )
                    if (index == values.size - 1) {
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun GenresLine(
    values: List<GenreItemResponse>,
    current: List<Int>,
    onSelected: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 8.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "${stringResource(id = R.string.genre)} : ",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                values.forEach {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = it.name ?: "",
                        color = if (it.id in current) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            shadow = Shadow(
                                color = Color.Black,
                                offset = Offset(0f, 0f),
                                blurRadius = 0.5f
                            )
                        ),
                        modifier = Modifier
                            .background(
                                if (it.id in current) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSecondary,
                                RoundedCornerShape(50)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .clickable {
                                it.id?.let { id -> onSelected.invoke(id) }
                            },
                    )
                }
            }
        }
    }
}

@Composable
fun <T> FilterLine(
    name: String,
    values: List<T>,
    itemContent: @Composable (T, Int) -> Unit,
) {
    if (values.isNotEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 4.dp, bottom = 4.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "$name : ",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    values.forEachIndexed { index, t ->
                        Spacer(modifier = Modifier.width(8.dp))
                        itemContent.invoke(t, index)
                    }
                }
            }
        }
    }
}

@Composable
fun MovieItemView(
    modifier: Modifier = Modifier,
    posterUrl: String,
    bottomRight: String?,
    title: String,
    onExpandDetails: () -> Unit
) {
    Column(modifier = modifier
        .padding(4.dp)
        .clickable {
            onExpandDetails.invoke()
        }) {

        Box {
            ProgressiveGlowingImage(
                url = posterUrl,
                glow = true
            )
            if (!bottomRight.isNullOrEmpty()) Text(
                text = bottomRight,
                style = MaterialTheme.typography.bodyMedium.copy(
                    shadow = Shadow(
                        color = Color.Black
                    )
                ),
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = AppYellow,
                modifier = Modifier
                    .padding(4.dp)
                    .background(Color.White, shape = RoundedCornerShape(8.dp))
                    .padding(horizontal = 4.dp, vertical = 0.dp)
                    .align(Alignment.BottomEnd)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(
                shadow = Shadow(
                    color = Color.Black,
                    offset = Offset(0f, 0f),
                    blurRadius = 1f
                )
            ),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun MovieHistoryItemView(
    modifier: Modifier = Modifier,
    history: MediaHistory,
    onExpandDetails: () -> Unit
) {
    Row(
        modifier = modifier
            .padding(4.dp)
            .clickable {
                onExpandDetails.invoke()
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.fillMaxWidth(0.2f)) {
            ProgressiveGlowingImage(
                url = history.data?.image.toString(),
                glow = true
            )
        }
        Column(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .weight(1f)
        ) {
            Text(
                text = history.data?.title ?: "",
                style = MaterialTheme.typography.bodyMedium.copy(
                    shadow = Shadow(
                        color = Color.Black,
                        offset = Offset(0f, 0f),
                        blurRadius = 1f
                    )
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.primary,
            )

            Text(
                text = "Server : ${history.serverIdx + 1}",
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.primary,
            )

            Text(
                text = "Episode : ${history.index + 1}",
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.primary,
            )

            Text(
                text = "Position : ${history.position.makeTimeString()}",
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
fun PersonItemView(
    modifier: Modifier = Modifier,
    person: Person,
    onExpandDetails: (Person) -> Unit
) {
    Column(
        modifier = modifier
            .padding(6.dp)
            .clickable {
                onExpandDetails.invoke(person)
            }, horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val posterUrl = Api.getPosterPath(person.profilePath)
        CircleGlowingImage(
            url = posterUrl,
            glow = true
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = person.name ?: "",
            style = MaterialTheme.typography.bodyMedium.copy(
                shadow = Shadow(
                    color = Color.Black,
                    offset = Offset(0f, 0f),
                    blurRadius = 1f
                )
            ),
            fontSize = 13.sp,
            maxLines = 2,
            textAlign = TextAlign.Center,
            lineHeight = 14.sp,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = person.knownForDepartment ?: "",
            style = MaterialTheme.typography.bodyMedium.copy(
                shadow = Shadow(
                    color = Color.Black,
                    offset = Offset(0f, 0f),
                    blurRadius = 1f
                )
            ),
            color = MaterialTheme.colorScheme.secondary,
            fontSize = 12.sp,
            maxLines = 2,
            lineHeight = 14.sp,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis
        )
    }
}