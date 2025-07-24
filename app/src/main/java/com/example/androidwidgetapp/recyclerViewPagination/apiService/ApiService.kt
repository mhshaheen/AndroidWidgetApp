package com.example.androidwidgetapp.recyclerViewPagination.apiService

import com.example.androidwidgetapp.recyclerViewPagination.model.Post
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("posts")
    suspend fun getPosts(
        @Query("_page") page: Int,
        @Query("_limit") limit: Int
    ): List<Post>

    @GET("posts")
    suspend fun getPostsList(): List<Post>
}