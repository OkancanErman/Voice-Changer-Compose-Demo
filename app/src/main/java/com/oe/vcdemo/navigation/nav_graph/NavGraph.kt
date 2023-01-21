package com.oe.vcdemo.navigation.nav_graph

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.oe.vcdemo.navigation.ROOT_GRAPH_ROUTE
import com.oe.vcdemo.navigation.VC_GRAPH_ROUTE

@Composable
fun SetupNavGraph(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = VC_GRAPH_ROUTE,
        route = ROOT_GRAPH_ROUTE
    ) {
        VoiceChangerNavGraph(navController)
    }
}