package com.oe.vcdemo.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.navigation.compose.rememberNavController
import com.oe.vcdemo.navigation.nav_graph.SetupNavGraph
import com.oe.vcdemo.viewmodels.RecordViewModel
import com.oe.vcdemo.ui.theme.VoiceChangerDemoTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val recordViewModel: RecordViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VoiceChangerDemoTheme {
                val navController = rememberNavController()
                SetupNavGraph(navController = navController)
                recordViewModel.createFolders(this)
            }
        }
    }

}