package com.petros.efthymiou.dailypulse.android.ui.screens

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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
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
import coil.compose.AsyncImage
import com.petros.efthymiou.dailypulse.articles.application.Article
import com.petros.efthymiou.dailypulse.articles.presentation.ArticlesViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun NativeArticlesScreen(
    onSourcesClick: () -> Unit,
    onAboutClick: () -> Unit,
    viewModel: ArticlesViewModel = koinViewModel(),
) {
    val state by viewModel.articlesState.collectAsState()

    Column {
        ArticlesAppBar(onSourcesClick = onSourcesClick, onAboutClick = onAboutClick)

        state.error?.let { NativeErrorMessage(it) }

        if (state.articles.isNotEmpty()) {
            ArticlesPullToRefreshList(
                articles = state.articles,
                refreshing = state.loading,
                onRefresh = { viewModel.getArticles(forceFetch = true) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArticlesAppBar(
    onSourcesClick: () -> Unit,
    onAboutClick: () -> Unit,
) {
    TopAppBar(
        title = { Text(text = "Articles") },
        actions = {
            IconButton(onClick = onSourcesClick) {
                Icon(imageVector = Icons.Filled.List, contentDescription = "Sources")
            }
            IconButton(onClick = onAboutClick) {
                Icon(imageVector = Icons.Filled.Info, contentDescription = "About Device")
            }
        },
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ArticlesPullToRefreshList(
    articles: List<Article>,
    refreshing: Boolean,
    onRefresh: () -> Unit,
) {
    val state = rememberPullRefreshState(refreshing = refreshing, onRefresh = onRefresh)

    Box(modifier = Modifier.pullRefresh(state)) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(articles) { article -> NativeArticleItem(article) }
        }
        PullRefreshIndicator(
            refreshing = refreshing,
            state = state,
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}

@Composable
private fun NativeArticleItem(article: Article) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        AsyncImage(
            model = article.imageUrl,
            contentDescription = "article image",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = article.title,
            style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 22.sp),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = article.desc)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = article.date,
            style = TextStyle(color = Color.Gray),
            modifier = Modifier.align(Alignment.End),
        )
    }
}
