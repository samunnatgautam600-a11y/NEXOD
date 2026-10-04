package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.OutlinedFlag
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.SearchMode
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.QuizScreen
import com.example.ui.screens.ResourcesScreen
import com.example.ui.screens.SavedScreen
import com.example.ui.screens.SearchLoadingScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TopicDetailScreen
import com.example.ui.theme.NexoraTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            NexoraTheme {
                NexoraApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun NexoraApp(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    val showBottomBar = uiState.currentScreen in listOf(
        Screen.HOME,
        Screen.SAVED,
        Screen.SETTINGS
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("bottom_nav_bar"),
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    NavigationBarItem(
                        selected = uiState.currentScreen == Screen.HOME,
                        onClick = { viewModel.navigateTo(Screen.HOME) },
                        icon = {
                            Icon(
                                imageVector = if (uiState.currentScreen == Screen.HOME) Icons.Default.Explore else Icons.Outlined.Explore,
                                contentDescription = "Home Search"
                            )
                        },
                        label = { Text("Search") },
                        modifier = Modifier.testTag("nav_search")
                    )

                    NavigationBarItem(
                        selected = uiState.currentScreen == Screen.SAVED,
                        onClick = { viewModel.navigateTo(Screen.SAVED) },
                        icon = {
                            Icon(
                                imageVector = if (uiState.currentScreen == Screen.SAVED) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Saved Library"
                            )
                        },
                        label = { Text("Saved") },
                        modifier = Modifier.testTag("nav_saved")
                    )

                    NavigationBarItem(
                        selected = uiState.currentScreen == Screen.SETTINGS,
                        onClick = { viewModel.navigateTo(Screen.SETTINGS) },
                        icon = {
                            Icon(
                                imageVector = if (uiState.currentScreen == Screen.SETTINGS) Icons.Default.Settings else Icons.Outlined.Settings,
                                contentDescription = "Settings"
                            )
                        },
                        label = { Text("Settings") },
                        modifier = Modifier.testTag("nav_settings")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState.currentScreen) {
                Screen.HOME -> HomeScreen(uiState = uiState, viewModel = viewModel)
                Screen.SEARCH_LOADING -> SearchLoadingScreen(uiState = uiState, viewModel = viewModel)
                Screen.TOPIC_DETAIL -> {
                    if (uiState.searchMode == SearchMode.RESOURCES && uiState.resourceResults.isNotEmpty()) {
                        ResourcesScreen(uiState = uiState, viewModel = viewModel)
                    } else if (uiState.currentTopic != null) {
                        TopicDetailScreen(
                            topic = uiState.currentTopic!!,
                            uiState = uiState,
                            viewModel = viewModel
                        )
                    } else {
                        HomeScreen(uiState = uiState, viewModel = viewModel)
                    }
                }
                Screen.QUIZ -> QuizScreen(uiState = uiState, viewModel = viewModel)
                Screen.SAVED -> {
                    BackHandler { viewModel.navigateTo(Screen.HOME) }
                    SavedScreen(uiState = uiState, viewModel = viewModel)
                }
                Screen.SETTINGS -> {
                    BackHandler { viewModel.navigateTo(Screen.HOME) }
                    SettingsScreen(uiState = uiState, viewModel = viewModel)
                }
            }
        }
    }
}
