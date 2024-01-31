package com.android.movieapp.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.paging.PagingData
import com.android.movieapp.models.entities.Person
import com.android.movieapp.network.Api
import com.android.movieapp.repository.FavoriteRepository
import com.android.movieapp.ui.ext.CircleGlowingImage
import com.android.movieapp.usecase.PopularUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


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

@HiltViewModel
class PersonScreenViewModel @Inject constructor(private val popularUseCase: PopularUseCase) :
    BaseSearchViewModel<Person>() {

    override val invoke: (String) -> Flow<PagingData<Person>> = {
        popularUseCase.invokePerson(it)
    }
}

@HiltViewModel
class FavoritePersonViewModel @Inject constructor(favoriteRepository: FavoriteRepository) :
    ViewModel() {

    val favoritePerson = favoriteRepository.favoritePersons()
}


