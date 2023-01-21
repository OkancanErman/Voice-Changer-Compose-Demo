package com.oe.vcdemo.navigation

const val ROOT_GRAPH_ROUTE = "root"
const val VC_GRAPH_ROUTE = "vc_graph"

sealed class Screen(val route: String) {
    object Play : Screen(route = "play_screen")
    object Record: Screen(route = "record_screen?changeRecord={changeRecord}"){
        fun passChangeRecord(
            changeRecord: Boolean = false
        ): String {
            return "record_screen?changeRecord=$changeRecord"
        }
    }
}