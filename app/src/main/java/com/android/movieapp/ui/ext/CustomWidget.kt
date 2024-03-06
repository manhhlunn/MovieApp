package com.android.movieapp.ui.ext

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp

@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    hint: String = "",
    value: String = "",
    onSearch: (String) -> Unit = {},
) {
    var isHintDisplayed by rememberSaveable { mutableStateOf(hint != "") }

    Box(modifier = modifier) {
        BasicTextField(
            value = value,
            onValueChange = {
                onSearch.invoke(it)
            },
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            maxLines = 1,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.onPrimary)
                .padding(start = 48.dp, top = 12.dp, bottom = 12.dp, end = 24.dp)
                .onFocusChanged {
                    isHintDisplayed = (!it.hasFocus && value.isEmpty())
                },
            textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.primary)
        )

        Icon(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(horizontal = 16.dp),
            painter = rememberVectorPainter(Icons.Default.Search),
            contentDescription = "Search icon"
        )

        if (isHintDisplayed) {
            Text(
                text = hint,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(
                    start = 48.dp,
                    top = 12.dp,
                    bottom = 12.dp,
                    end = 24.dp
                )
            )
        }
    }
}
