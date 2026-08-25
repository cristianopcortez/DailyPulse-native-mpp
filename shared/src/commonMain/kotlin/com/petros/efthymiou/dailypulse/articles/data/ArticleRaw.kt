package com.petros.efthymiou.dailypulse.articles.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ArticleRaw(
    @SerialName("title")
    val title: String,
    @SerialName("desc")
    val desc: String = "",
    @SerialName("date")
    val date: String,
    @SerialName("imageUrl")
    val imageUrl: String = "",
)
