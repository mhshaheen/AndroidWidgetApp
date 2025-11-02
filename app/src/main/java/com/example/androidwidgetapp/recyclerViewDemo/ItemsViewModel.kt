package com.example.androidwidgetapp.recyclerViewDemo

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ItemsViewModel(
    @SerialName("image")
    val image: Int,
    @SerialName("text")
    val text: String
)