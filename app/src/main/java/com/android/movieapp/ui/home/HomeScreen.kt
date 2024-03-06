package com.android.movieapp.ui.home

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.android.movieapp.NavScreen
import com.android.movieapp.ui.configure.ConfigureScreen
import com.android.movieapp.ui.configure.CountryViewModel
import com.android.movieapp.ui.configure.LanguageViewModel
import com.android.movieapp.ui.home.widget.AppDrawerContent
import com.android.movieapp.ui.home.widget.HomeDrawerNavigation
import com.android.movieapp.ui.home.widget.TopAppBar
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
        Scaffold(
            topBar = {
                TopAppBar(onSettingsClicked = {
                    coroutineScope.launch {
                        drawerState.open()
                    }
                })
            }
        ) {
            NavHost(
                route = NavScreen.HomeScreen.route,
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
                    MediaScreen(navController = navController)
                }
            }
        }
    }
}



