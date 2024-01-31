package com.android.movieapp.ui.ext

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import coil.compose.AsyncImage


@Composable
fun FilterRow(
    resId: Int,
    items: List<DropdownItem>,
    selected: DropdownItem,
    onSelected: (DropdownItem) -> Unit,
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        var showDropdown by remember { mutableStateOf(false) }
        Text(
            text = "${stringResource(id = resId)} :",
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Box {
            ToggleContent(selected.name, selected.url) {
                showDropdown = true
            }
            DropdownMenu(
                expanded = showDropdown,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .background(MaterialTheme.colorScheme.background),
                onDismissRequest = { showDropdown = false }
            ) {
                items.forEach { item ->
                    val isSelected = item == selected
                    DropdownItemView(item.name, item.url, isSelected) {
                        onSelected(item)
                        showDropdown = false
                    }
                }
            }
        }
    }
}


private const val FLAG_ID = "flag"
private const val TICK_ID = "tickIcon"
private const val DROPDOWN_ID = "dropdownIcon"
private val placeholder = Placeholder(
    width = 2.5.em,
    height = 1.5.em,
    placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter,
)

@Composable
private fun ToggleContent(countryName: String, flagUrl: String, onClick: () -> Unit) {
    val flagContent = flagContent(flagUrl, countryName)
    val arrowContent = iconContent(DROPDOWN_ID, Icons.Default.ArrowDropDown)
    Text(
        text = buildAnnotatedString {
            appendInlineContent(FLAG_ID)
            append("  $countryName")
            appendInlineContent(DROPDOWN_ID)
        },
        inlineContent = mapOf(arrowContent, flagContent),
        modifier = Modifier.clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.primary
    )
}


@Composable
private fun DropdownItemView(
    name: String,
    flagUrl: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = {
            Text(
                text = buildAnnotatedString {
                    if (isSelected) {
                        appendInlineContent(TICK_ID)
                    }
                    appendInlineContent(FLAG_ID)
                    append("  $name")
                },
                inlineContent = inlineContent(flagUrl, name, isSelected),
                color = MaterialTheme.colorScheme.primary
            )
        },
        enabled = !isSelected,
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
    )
}

private fun inlineContent(
    flagUrl: String,
    countryName: String,
    selected: Boolean
): Map<String, InlineTextContent> {
    val flagContent = flagContent(flagUrl, countryName)
    return if (selected) mapOf(iconContent(TICK_ID, Icons.Default.Done), flagContent) else mapOf(
        flagContent
    )
}

private fun flagContent(flagUrl: String, countryName: String) = FLAG_ID to InlineTextContent(
    placeholder = placeholder,
    children = {
        AsyncImage(
            model = flagUrl,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize(),
            contentDescription = countryName,
        )
    },
)

private fun iconContent(id: String, icon: ImageVector) = id to InlineTextContent(
    placeholder = placeholder,
    children = {
        Image(
            imageVector = icon,
            contentDescription = null,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
        )
    },
)

data class DropdownItem(
    val value: String = "",
    val name: String = "Not selected",
    val url: String = ""
)