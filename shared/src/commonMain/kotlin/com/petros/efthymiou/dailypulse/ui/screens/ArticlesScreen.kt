package com.petros.efthymiou.dailypulse.ui.screens


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.petros.efthymiou.dailypulse.articles.application.Article
import com.petros.efthymiou.dailypulse.articles.presentation.ArticlesViewModel
import com.petros.efthymiou.dailypulse.ui.screens.elements.ErrorMessage
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import io.ktor.http.Url
import org.koin.compose.koinInject

class ArticlesScreen : Screen {

    @Composable
    override fun Content() {
        ArticlesScreenContent()
    }
}

@Composable
fun ArticlesScreenContent(
    articlesViewModel: ArticlesViewModel = koinInject(),
) {
    val articlesState by articlesViewModel.articlesState.collectAsState()

    Column {
        AppBar()

        articlesState.error?.let { ErrorMessage(it) }
        when {
            articlesState.loading && articlesState.articles.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            articlesState.articles.isNotEmpty() -> {
                ArticlesListView(viewModel = articlesViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppBar() {
    val navigator = LocalNavigator.currentOrThrow

    TopAppBar(
        title = { Text(text = "Articles") },
        actions = {
            IconButton(onClick = {
                navigator.push(AggregatorScreen())
            }) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = "News Provider Button",
                )
            }
            IconButton(onClick = {
                navigator.push(SourcesScreen())
            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.List,
                    contentDescription = "Sources Button",
                )
            }
            IconButton(onClick = {
                navigator.push(AboutScreen())
            }) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = "About Device Button",
                )
            }
        }
    )
}

@Composable
@OptIn(ExperimentalMaterialApi::class)
fun ArticlesListView(viewModel: ArticlesViewModel) {
    val articlesState by viewModel.articlesState.collectAsState()
    val pullRefreshState = rememberPullRefreshState(
        refreshing = articlesState.loading,
        onRefresh = { viewModel.getArticles(true) }
    )

    Box(
        modifier = Modifier.pullRefresh(state = pullRefreshState)
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(articlesState.articles) { article ->
                ArticleItemView(article = article)
            }
        }
        PullRefreshIndicator(
            refreshing = articlesState.loading,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
fun ArticleItemView(article: Article) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        if (article.imageUrl.isNotBlank()) {
            KamelImage(
                resource = asyncPainterResource(data = Url(article.imageUrl)),
                contentDescription = "article image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                onFailure = { },
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = article.title,
            style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 22.sp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = article.desc)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = article.date,
            style = TextStyle(color = Color.Gray),
            modifier = Modifier.align(Alignment.End)
        )
        Spacer(modifier = Modifier.height(4.dp))
    }
}






