package com.example.androidwidgetapp.recyclerViewPagination.model

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Keep
@Entity(tableName = "posts")
data class Post(
    @SerializedName("userId")
    val userId: Int,
    @PrimaryKey
    @SerializedName("id")
    val id: Int,
    @SerializedName("title")
    val title: String,
    @SerializedName("body")
    val body: String,
    @SerializedName("isFav")
    var isFav: Boolean = false
): Serializable