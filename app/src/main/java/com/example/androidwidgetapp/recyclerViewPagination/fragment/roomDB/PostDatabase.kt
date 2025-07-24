//package com.example.androidwidgetapp.recyclerViewPagination.fragment.roomDB
//
//import android.content.Context
//import androidx.room.Database
//import androidx.room.Room
//import androidx.room.RoomDatabase
//import com.example.androidwidgetapp.recyclerViewPagination.model.Post
//import dagger.Provides
//
//@Database(entities = [Post::class], version = 1, exportSchema = false)
//abstract class PostDatabase : RoomDatabase() {
//
//    abstract fun postDao(): PostDao
//
//    companion object {
//        @Volatile
//        private var INSTANCE: PostDatabase? = null
//
//        fun getDatabase(context: Context): PostDatabase {
//            return INSTANCE ?: synchronized(this) {
//                val instance = Room.databaseBuilder(
//                    context.applicationContext,
//                    PostDatabase::class.java,
//                    "post_database"
//                ).build()
//                INSTANCE = instance
//                instance
//            }
//        }
//    }
//}