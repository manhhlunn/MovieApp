package com.android.movieapp.ui.home

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.android.movieapp.R
import com.android.movieapp.ui.configure.ConfigureScreen
import com.android.movieapp.ui.configure.CountryViewModel
import com.android.movieapp.ui.configure.LanguageViewModel
import kotlinx.coroutines.launch


@Composable
fun HomeScreen(navController: NavController) {
    val navControllerHome = rememberNavController()
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    ModalNavigationDrawer(drawerContent = {
        AppDrawerContent(
            drawerState = drawerState,
            menuItems = HomeDrawerNavigation.entries,
            navController = navControllerHome
        )
    }, drawerState = drawerState) {
        Scaffold(topBar = {
            TopAppBar(onSettingsClicked = {
                coroutineScope.launch {
                    drawerState.open()
                }
            })
        }) {
            NavHost(
                modifier = Modifier.padding(it),
                navController = navControllerHome,
                startDestination = HomeDrawerNavigation.PopularScreen.route
            ) {
                composable(route = HomeDrawerNavigation.RegionScreen.route) {
                    ConfigureScreen(
                        hiltViewModel<CountryViewModel>()
                    )
                }

                composable(route = HomeDrawerNavigation.LanguageScreen.route) {
                    ConfigureScreen(
                        hiltViewModel<LanguageViewModel>()
                    )
                }

                composable(route = HomeDrawerNavigation.FavoriteScreen.route) {
                    FavoriteScreen(navController = navController)
                }

                composable(route = HomeDrawerNavigation.WatchedScreen.route) {
                    WatchedScreen(navController = navController)
                }

                composable(route = HomeDrawerNavigation.FilterScreen.route) {
                    FilterScreen(navController = navController)
                }

                composable(route = HomeDrawerNavigation.PopularScreen.route) {
                    PopularScreen(navController = navController)
                }

                composable(route = HomeDrawerNavigation.MediaScreen.route) {
                    OMovieScreen(navController = navController)
                }
            }
        }
    }
}

enum class HomeDrawerNavigation(
    val route: String,
    @DrawableRes val icon: Int,
    @StringRes val title: Int
) {
    PopularScreen("popular_screen", R.drawable.ic_home, R.string.home_title),
    FavoriteScreen("fav_screen", R.drawable.ic_fav, R.string.fav_title),
    WatchedScreen("watched_screen", R.drawable.ic_lib, R.string.watched),
    FilterScreen("filter_screen", R.drawable.ic_filter, R.string.filter_title),
    MediaScreen("media_screen", R.drawable.movie_ic, R.string.media),
    RegionScreen("region_screen", R.drawable.ic_country, R.string.country_title),
    LanguageScreen("language_screen", R.drawable.ic_language, R.string.language_title),
}


@Composable
fun AppDrawerContent(
    drawerState: DrawerState,
    menuItems: List<HomeDrawerNavigation>,
    navController: NavController
) {
    // default home destination to avoid duplication
    val coroutineScope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    ModalDrawerSheet {
        Column(
            horizontalAlignment = CenterHorizontally,
            modifier = Modifier
                .align(CenterHorizontally)
                .padding(horizontal = 20.dp)
                .width(200.dp)
        ) {
            val interactionSource = remember { MutableInteractionSource() }
            Image(
                painter = painterResource(id = R.drawable.ic_film),
                contentDescription = "Main app icon",
                modifier = Modifier.fillMaxWidth()
            )
            // column of options to pick from for user
            LazyColumn(
                horizontalAlignment = CenterHorizontally
            ) {
                items(menuItems.size) { idx ->
                    AppDrawerItem(
                        item = menuItems[idx],
                        menuItems[idx].route == currentRoute,
                        interactionSource
                    ) { navOption ->
                        // if it is the same - ignore the click
                        if (currentRoute == navOption.route) {
                            return@AppDrawerItem
                        }

                        // close the drawer after clicking the option
                        coroutineScope.launch {
                            drawerState.close()
                        }
                        // navigate to the required screen
                        navController.navigate(navOption.route) {
                            navController.graph.startDestinationRoute?.let { screenRoute ->
                                popUpTo(screenRoute) {
                                    saveState = true
                                }
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun AppDrawerItem(
    item: HomeDrawerNavigation,
    isSelected: Boolean,
    interactionSource: MutableInteractionSource,
    onClick: (options: HomeDrawerNavigation) -> Unit
) =
    Row(
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(vertical = 4.dp)
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(48))
            .background(
                if (isSelected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onPrimary
            )
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .clickable(
                onClick = { onClick(item) },
                interactionSource = interactionSource,
                indication = null
            ),
    ) {
        Icon(
            painter = painterResource(id = item.icon),
            contentDescription = stringResource(id = item.title),
            modifier = Modifier
                .size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = stringResource(id = item.title),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 16.sp
            ),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary
        )
    }



