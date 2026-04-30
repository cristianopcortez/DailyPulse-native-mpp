package com.petros.efthymiou.dailypulse.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.petros.efthymiou.dailypulse.ui.App
import com.petros.efthymiou.dailypulse.ui.MyApplicationTheme

/**
 * Entry point for the **mpp** product flavor.
 *
 * Hosts the Compose Multiplatform `App()` composable defined in `:shared`, so
 * the exact same UI tree runs on Android, iOS, Desktop and Web targets.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    App()
                }
            }
        }
    }
}
