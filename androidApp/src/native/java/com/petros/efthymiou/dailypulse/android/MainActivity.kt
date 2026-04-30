package com.petros.efthymiou.dailypulse.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.petros.efthymiou.dailypulse.android.ui.theme.DailyPulseTheme
import com.petros.efthymiou.dailypulse.android.ui.screens.NativeAboutScreen
import com.petros.efthymiou.dailypulse.android.ui.screens.NativeArticlesScreen
import com.petros.efthymiou.dailypulse.android.ui.screens.NativeSourcesScreen

/**
 * Entry point for the **native** product flavor.
 *
 * The UI is implemented purely in Jetpack Compose (no Compose Multiplatform).
 * Only the business layer (`ArticlesViewModel`, `SourcesViewModel`,
 * use cases, repositories, Koin modules…) is reused from `:shared`.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DailyPulseTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "articles") {
                        composable("articles") {
                            NativeArticlesScreen(
                                onSourcesClick = { navController.navigate("sources") },
                                onAboutClick = { navController.navigate("about") },
                            )
                        }
                        composable("sources") {
                            NativeSourcesScreen(onBack = { navController.popBackStack() })
                        }
                        composable("about") {
                            NativeAboutScreen(onBack = { navController.popBackStack() })
                        }
                    }
                }
            }
        }
    }
}
