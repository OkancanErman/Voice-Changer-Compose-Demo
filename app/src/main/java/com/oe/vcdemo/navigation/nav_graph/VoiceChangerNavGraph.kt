package com.oe.vcdemo.navigation.nav_graph

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.oe.vcdemo.ui.RecordScreen
import com.oe.vcdemo.navigation.Screen
import com.oe.vcdemo.navigation.VC_GRAPH_ROUTE
import com.oe.vcdemo.ui.PlayScreen
import com.oe.vcdemo.viewmodels.PlayViewModel
import com.oe.vcdemo.viewmodels.RecordViewModel

@OptIn(ExperimentalFoundationApi::class)
fun NavGraphBuilder.VoiceChangerNavGraph(
    navController: NavHostController
){
    navigation(
        startDestination = Screen.Record.route,
        route = VC_GRAPH_ROUTE
    ) {
        composable(
            route = Screen.Play.route
        ) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(route = Screen.Play.route)
            }
            val viewModel = hiltViewModel<PlayViewModel>(parentEntry)

            PlayScreen(
                viewModel,
            ) { changeRecord ->
                navController.navigate(route = Screen.Record.passChangeRecord(changeRecord))
            }
        }
        composable(
            route = Screen.Record.route,
            arguments = listOf(navArgument("changeRecord") { defaultValue = false })
        ) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(route = Screen.Record.route)
            }
            val viewModel = hiltViewModel<RecordViewModel>(parentEntry)

            RecordScreen(
                backStackEntry.arguments?.getBoolean("changeRecord")!!,
                viewModel,
            ) {
                navController.navigate(route = Screen.Play.route)
            }
        }
    }
}