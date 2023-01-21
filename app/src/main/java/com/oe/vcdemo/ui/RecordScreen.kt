package com.oe.vcdemo.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.oe.vcdemo.viewmodels.RecordViewModel
import com.oe.vcdemo.ui.theme.DeepBlue
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.oe.vcdemo.helper.StopWatch


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RecordScreen(
    changeRecord: Boolean = false,
    viewModel: RecordViewModel = hiltViewModel(),
    onNavigateToPlay: () -> Unit
) {
    val context = LocalContext.current
    val stopWatch = remember { StopWatch() }

    var changeRecord by remember {
        mutableStateOf(changeRecord)
    }

    val recordingState by viewModel.recordingState.collectAsState(false)

    viewModel.createFolders(context)

    if (viewModel.checkWavExist() && changeRecord.not() && recordingState.not()) {
        onNavigateToPlay()
    }else {
        // RECORD_AUDIO permission state
        val recordPermissionState = rememberPermissionState(
            android.Manifest.permission.RECORD_AUDIO
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DeepBlue),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RecordText(stopWatch)

            IconButton(
                modifier = Modifier
                    .then(Modifier.size(192.dp))
                    .border(
                        8.dp,
                        if (recordingState.not()) Color.Green else Color.Red,
                        shape = CircleShape
                    ),
                onClick = {

                    when (recordPermissionState.status) {
                        PermissionStatus.Granted -> {
                            if (recordingState.not()) {
                                viewModel.startRecording()
                                stopWatch.start()
                            } else {
                                viewModel.stopRecording()
                                stopWatch.reset()
                                onNavigateToPlay()
                            }
                        }
                        is PermissionStatus.Denied -> {
                            recordPermissionState.launchPermissionRequest()
                        }
                    }

                },
            ) {
                Icon(
                    Icons.Filled.Mic,
                    "Recording microphone",
                    tint = if (recordingState.not()) Color.Green else Color.Red,
                    modifier = Modifier.size(128.dp)
                )
            }
        }

        val lifecycleOwner = LocalLifecycleOwner.current
        DisposableEffect(lifecycleOwner) {

            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_START) {
                    if (viewModel.checkWavExist() && changeRecord.not()) {
                        onNavigateToPlay()
                    }
                } else if (event == Lifecycle.Event.ON_STOP) {
                    if (recordingState) {
                        viewModel.stopRecording()
                        stopWatch.reset()
                        changeRecord = true
                    }
                }
            }

            lifecycleOwner.lifecycle.addObserver(observer)

            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }
    }
}

@Composable
fun RecordText(stopWatch: StopWatch) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Click to record",
                style = MaterialTheme.typography.h2
            )
            StopWatchDisplay(
                formattedTime = stopWatch.formattedTime,
            )
        }
}

@Composable
fun StopWatchDisplay(
    formattedTime: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = formattedTime,
            fontWeight = FontWeight.Bold,
            fontSize = 30.sp,
            color = Color.White
        )
    }
}