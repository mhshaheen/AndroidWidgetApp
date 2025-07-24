package com.example.androidwidgetapp.recyclerViewDemo

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ItemsViewModel(
    @SerializedName("image")
    val image: Int,
    @SerializedName("text")
    val text: String
) : Serializable