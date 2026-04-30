package com.petros.efthymiou.dailypulse.android.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.petros.efthymiou.dailypulse.Platform

@Composable
fun NativeAboutScreen(onBack: () -> Unit) {
    Column {
        AboutAppBar(onBack = onBack)
        AboutContent()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AboutAppBar(onBack: () -> Unit) {
    TopAppBar(
        title = { Text(text = "About Device") },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                )
            }
        },
    )
}

@Composable
private fun AboutContent() {
    val rows = buildAboutRows()
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(rows) { (title, subtitle) -> AboutRow(title, subtitle) }
    }
}

private fun buildAboutRows(): List<Pair<String, String>> {
    val platform = Platform()
    platform.logSystemInfo()
    return listOf(
        "Operating System" to "${platform.osName} ${platform.osVersion}",
        "Device" to platform.deviceModel,
        "Density" to platform.density.toString(),
    )
}

@Composable
private fun AboutRow(title: String, subtitle: String) {
    Column(Modifier.padding(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
    Divider()
}
