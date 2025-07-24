//package com.example.androidwidgetapp.recyclerViewPagination.repository
//
//import android.util.Log
//import androidx.lifecycle.LiveData
//import androidx.paging.Pager
//import androidx.paging.PagingConfig
//import androidx.paging.PagingSource
//import androidx.paging.PagingState
//import com.example.androidwidgetapp.recyclerViewPagination.apiService.ApiService
//import com.example.androidwidgetapp.recyclerViewPagination.fragment.roomDB.PostDao
//import com.example.androidwidgetapp.recyclerViewPagination.model.Post
//import javax.inject.Inject
//
//class PostRepository @Inject constructor (
//    private val apiService: ApiService,
//    private val postDao: PostDao
//) {
//
//    fun getPosts(): Pager<Int, Post> {
//        return Pager(
//            config = PagingConfig(pageSize = 10, /*initialLoadSize = 10,*/enablePlaceholders = false),
//            pagingSourceFactory = { PostPagingSource(apiService) }
//        )
//    }
//
//    inner class PostPagingSource(private val apiService: ApiService) : PagingSource<Int, Post>() {
//        override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Post> {
//            return try {
//                val currentPage = params.key ?: 1
//                val response = apiService.getPosts(page = currentPage, limit = params.loadSize)
//                LoadResult.Page(
//                    data = response,
//                    prevKey = if (currentPage == 1) null else currentPage - 1,
//                    nextKey = if (response.isEmpty()) null else currentPage + 1
//                )
//            } catch (e: Exception) {
//                LoadResult.Error(e)
//            }
//        }
//
//        override fun getRefreshKey(state: PagingState<Int, Post>): Int? {
//            return state.anchorPosition?.let { anchorPosition ->
//                state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
//                    ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
//            }
//        }
//    }
//
//    suspend fun getPostsList() : List<Post> {
//        return apiService.getPostsList()
//    }
//
//    /*********************************Room Database************************************/
//    val allPosts: LiveData<List<Post>> = postDao.getAllPosts()
//    val favoritePosts: LiveData<List<Post>> = postDao.getFavoritePosts()
//
//    suspend fun insert(post: Post) {
//        postDao.insertPost(post)
//    }
//
//    suspend fun update(post: Post) {
//        postDao.updatePost(post)
//    }
//
//    suspend fun delete(post: Post) {
//        postDao.deletePost(post)
//    }
//
//    suspend fun getPostById(postId: Int): Post {
//        return postDao.getPostById(postId)
//    }
//}