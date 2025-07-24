//package com.example.androidwidgetapp.recyclerViewPagination.fragment.roomDB
//
//import androidx.lifecycle.LiveData
//import androidx.room.Dao
//import androidx.room.Delete
//import androidx.room.Insert
//import androidx.room.OnConflictStrategy
//import androidx.room.Query
//import androidx.room.Update
//import com.example.androidwidgetapp.recyclerViewPagination.model.Post
//
//@Dao
//interface PostDao {
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertPost(post: Post)
//
//    @Delete
//    suspend fun deletePost(post: Post)
//
//    @Update
//    suspend fun updatePost(post: Post)
//
//    @Query("SELECT * FROM posts WHERE isFav = 1")
//    fun getFavoritePosts(): LiveData<List<Post>>
//
//    @Query("SELECT * FROM posts")
//    fun getAllPosts(): LiveData<List<Post>>
//
//    @Query("SELECT * FROM posts WHERE id = :postId")
//    suspend fun getPostById(postId: Int): Post
//}