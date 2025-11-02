package com.example.androidwidgetapp.recyclerViewPagination.model

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Entity(tableName = "posts")
@Serializable
data class Post(
    @SerialName("userId")
    val userId: Int,
    @PrimaryKey
    @SerialName("id")
    val id: Int,
    @SerialName("title")
    val title: String,
    @SerialName("body")
    val body: String,
    @SerialName("isFav")
    var isFav: Boolean = false
)