package com.oe.vcdemo.ui

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.placeholder
import com.google.accompanist.placeholder.shimmer
import com.oe.vcdemo.R
import com.oe.vcdemo.viewmodels.PlayViewModel
import com.oe.vcdemo.ui.theme.*

data class ItemContent(
    val title: String,
    @DrawableRes val iconId: Int
)

@ExperimentalFoundationApi
@Composable
fun PlayScreen(
    viewModel: PlayViewModel = hiltViewModel(),
    onNavigateToRecord: (Boolean) -> Unit
)
{
    var selectedTabIndex by remember {
        mutableStateOf(0)
    }
    val scrollState = rememberScrollState()
    val scaffoldState = rememberScaffoldState()

    val context = LocalContext.current

    Scaffold(
            Modifier.background(DeepBlue),
            scaffoldState = scaffoldState,
            bottomBar = {
                BottomMenuTabView(
                    items = listOf(
                        ItemContent("Home", R.drawable.ic_home),
                        ItemContent("Records", R.drawable.ic_music),
                        ItemContent("Settings", R.drawable.ic_baseline_settings),
                    )
                ) {
                    selectedTabIndex = it
                }
            }
        ) {

        Column(modifier = Modifier.padding(bottom = it.calculateBottomPadding())) {

            BackToRecord(onNavigateToRecord, viewModel)

            when(selectedTabIndex) {
                0 -> HomeSection(viewModel = viewModel,
                    items = listOf(
                        ItemContent("Normal", R.drawable.simple_smile),
                        ItemContent("Helium", R.drawable.ballon),
                        ItemContent("Hexafluoride", R.drawable.ballon_hexa),
                        ItemContent("Robot", R.drawable.robot),
                        ItemContent("Cave", R.drawable.cave),
                        ItemContent("Alien", R.drawable.alien),
                        ItemContent("Backwards", R.drawable.backward),
                        ItemContent("Monster", R.drawable.monster),
                        ItemContent("Grand", R.drawable.grandc),
                        ItemContent("Surprised", R.drawable.surprised),
                        ItemContent("Small Creature", R.drawable.smallc),
                        ItemContent("Deep Voice", R.drawable.deepv),
                        ItemContent("Telephone", R.drawable.telephone),
                        ItemContent("Nervous", R.drawable.nervous),
                        ItemContent("Dizzy", R.drawable.dizzy),
                        ItemContent("Duck", R.drawable.duck),
                        ItemContent("Giant", R.drawable.golem),
                        ItemContent("Cyborg", R.drawable.cyborg),
                        ItemContent("Squirrel", R.drawable.squirrel1),
                        ItemContent("Extraterrestrial", R.drawable.extraterrestrial),
                        ItemContent("Fan", R.drawable.fan),
                        ItemContent("Sheep", R.drawable.sheep),
                        ItemContent("Megaphone", R.drawable.megaphone1),
                        ItemContent("Dj", R.drawable.dj)
                    ),
                    scrollState
                )
                1 -> RecordsSection()
                2 -> SettingsSection()
            }
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {

            } else if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkAndGetWav(context)
            } else if (event == Lifecycle.Event.ON_STOP) {

            } else if (event == Lifecycle.Event.ON_DESTROY) {
                viewModel.onDestroy()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    var openDialog by remember { mutableStateOf(false) }

    BackHandler {
        openDialog = true
    }

    if (openDialog) {
        Dialog(
            onDismissRequest = { openDialog = false } ) {

            Card(
                elevation = 8.dp,
                shape = RoundedCornerShape(12.dp)
            ) {

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .background(DeepBlue)
                        .padding(8.dp)
                ) {

                    Text(
                        text = "Do you want to exit?",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(8.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Buttons
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            shape = RoundedCornerShape(10.dp),
                            onClick = { openDialog = false },
                            colors = ButtonDefaults.outlinedButtonColors(ButtonBlue)
                        ) {
                            Text(text = "No", color = Color.White)
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        OutlinedButton(
                            shape = RoundedCornerShape(10.dp),
                            onClick = {
                                openDialog = false
                                (context as Activity).finish()
                            },
                            colors = ButtonDefaults.outlinedButtonColors(ButtonBlue)
                        ){
                            Text(text = "Exit", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@ExperimentalFoundationApi
@Composable
fun HomeSection(
    viewModel: PlayViewModel,
    items: List<ItemContent>,
    scrollState: ScrollState
) {
    Column(modifier= Modifier
        .verticalScroll(scrollState)
        .fillMaxWidth()
        .fillMaxHeight()
    ){
        for(item in items){

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(15.dp)
            ){
                Image(
                    painter = painterResource(id = item.iconId),
                    contentDescription = "",
                    modifier = Modifier
                        .size(60.dp)
                        .placeholder(
                            visible = false,
                            color = Color.Gray,
                            shape = RoundedCornerShape(4.dp),
                            highlight = PlaceholderHighlight.shimmer(
                                highlightColor = Color.White,
                            )
                        )
                )

                Text(
                    text = item.title,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp)
                        .placeholder(
                            visible = false,
                            color = Color.Gray,
                            shape = RoundedCornerShape(4.dp),
                            highlight = PlaceholderHighlight.shimmer(
                                highlightColor = Color.White,
                            ),
                        )
                )

                val whichPlaying by viewModel.whichPlaying.collectAsState(-1)
                MusicButton(
                    whichPlaying == items.indexOf(item),
                    onButtonClick = {
                        viewModel.onButtonClick(items.indexOf(item))
                    }
                )

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(ButtonBlue)
                        .padding(10.dp)
                        .size(20.dp)
                        .placeholder(
                            visible = false,
                            color = Color.Gray,
                            shape = RoundedCornerShape(4.dp),
                            highlight = PlaceholderHighlight.shimmer(
                                highlightColor = Color.White,
                            )
                        ),
                    onClick = {

                    }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_baseline_more_vert_24),
                        contentDescription = "Play",
                        modifier = Modifier.size(20.dp),
                        colorFilter = ColorFilter.tint(color = Color.White)
                    )
                }
            }
        }
    }
}

@Composable
fun MusicButton(
    isPlaying: Boolean,
    onButtonClick: () -> Unit
) {
    IconButton(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(ButtonBlue)
            .padding(10.dp)
            .size(20.dp),
        onClick = onButtonClick,
    ) {
        if(!isPlaying) {
            SetButtonIcon(
                icon = Icons.Filled.PlayArrow,
                iconDescription = "Play"
            )
        } else {
            SetButtonIcon(
                Icons.Filled.Pause,
                iconDescription = "Pause"
            )
        }
    }
}

@Composable
private fun SetButtonIcon(
    icon: ImageVector,
    iconDescription: String
) {
    Icon(
        modifier = Modifier
            .size(20.dp),
        imageVector = icon,
        contentDescription = iconDescription,
        tint = Color.White
    )
}

@ExperimentalFoundationApi
@Composable
fun RecordsSection() {
    Box(modifier= Modifier
        .fillMaxWidth()
        .fillMaxHeight()
        .background(DeepBlue)){

    }
}

@ExperimentalFoundationApi
@Composable
fun SettingsSection() {
    Box(modifier= Modifier
        .fillMaxWidth()
        .fillMaxHeight()
        .background(DeepBlue)){

    }
}

@Composable
fun BottomMenuTabView(
    items: List<ItemContent>,
    activeHighlightColor: Color = ButtonBlue,
    activeTextColor: Color = Color.White,
    inactiveTextColor: Color = AquaBlue,
    initialSelectedItemIndex: Int = 0,
    onTabSelected: (selectedIndex: Int) -> Unit
) {
    var selectedTabIndex by remember {
        mutableStateOf(initialSelectedItemIndex)
    }
    TabRow(
        selectedTabIndex = selectedTabIndex,
        backgroundColor = Color.Transparent,
        divider = {TabRowDefaults.Divider(color = Color.Transparent)},
        indicator = {TabRowDefaults.Indicator(color = Color.Transparent)},
    ) {
        items.forEachIndexed { index, item ->
            BottomMenuTab(
                item = item,
                isSelected = index == selectedTabIndex,
                activeHighlightColor = activeHighlightColor,
                activeTextColor = activeTextColor,
                inactiveTextColor = inactiveTextColor,
                onItemClick = {
                    selectedTabIndex = index
                    onTabSelected(index)
                },
            )
        }
    }
}

@Composable
fun BottomMenuTab(
    item: ItemContent,
    isSelected: Boolean = false,
    activeHighlightColor: Color = ButtonBlue,
    activeTextColor: Color = Color.White,
    inactiveTextColor: Color = AquaBlue,
    onItemClick: () -> Unit
) {
    Tab(selected = isSelected, onClick = onItemClick) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(top=10.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) activeHighlightColor else Color.Transparent)
                    .padding(10.dp)
            ) {
                Icon(
                    painter = painterResource(id = item.iconId),
                    contentDescription = item.title,
                    tint = if(isSelected) activeTextColor else inactiveTextColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = item.title,
                color = if(isSelected) activeTextColor else inactiveTextColor
            )
        }
    }
}

@Composable
fun BackToRecord(
    onNavigateToRecord: (Boolean) -> Unit,
    viewModel: PlayViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(15.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        IconButton(
            modifier = Modifier
                .then(Modifier.size(128.dp))
                .border(
                    4.dp,
                    Color.Green,
                    shape = CircleShape
                ),
            onClick = {
                viewModel.stopPlaying()
                onNavigateToRecord(true)
            },
        ) {
            Icon(
                Icons.Filled.Mic,
                "Recording microphone",
                tint = Color.Green,
                modifier = Modifier.size(96.dp))
        }
    }
}

