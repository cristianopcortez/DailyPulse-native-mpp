package com.petros.efthymiou.dailypulse.sources.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SourceRaw(
    @SerialName("id")
    val id: String,
    @SerialName("name")
    val name: String,
    @SerialName("desc")
    val desc: String,
    @SerialName("origin")
    val origin: String,
)
