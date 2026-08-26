package com.petros.efthymiou.dailypulse.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.petros.efthymiou.dailypulse.sources.application.Source
import com.petros.efthymiou.dailypulse.sources.presentation.SourcesViewModel
import com.petros.efthymiou.dailypulse.ui.screens.elements.ErrorMessage
import org.koin.compose.koinInject

class SourcesScreen: Screen {

    @Composable
    override fun Content() {
        SourcesScreenContent()
    }
}

@Composable
fun SourcesScreenContent(
    viewModel: SourcesViewModel = koinInject()) {
    val sourcesState by viewModel.sourcesState.collectAsState()

    Column {
        AppBar()

        sourcesState.error?.let { ErrorMessage(it) }

        SourcesListView(viewModel)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppBar() {
    val navigator = LocalNavigator.currentOrThrow

    TopAppBar(
        title = { Text(text = "Sources") },
        navigationIcon = {
            IconButton(onClick = {
                navigator.pop()
            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Up Button",
                )
            }
        }
    )
}

@Composable
fun SourcesListView(viewModel: SourcesViewModel) {
    val sourcesState by viewModel.sourcesState.collectAsState()

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(sourcesState.sources) { source ->
            SourceItemView(source = source)
        }
    }
}

@Composable
fun SourceItemView(source: Source) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = source.name,
            style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 22.sp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = source.desc)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = source.origin,
            style = TextStyle(color = Color.Gray),
            modifier = Modifier.align(Alignment.End)
        )
        Spacer(modifier = Modifier.height(4.dp))
    }
}










