package com.example.androidwidgetapp.recyclerViewPagination.fragment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.androidwidgetapp.databinding.ItemPostBinding
import com.example.androidwidgetapp.recyclerViewPagination.model.Post

class PostAdapter : PagingDataAdapter<Post, PostAdapter.PostViewHolder>(PostDiffCallback()) {

    var itemClickListener : ((Post) -> Unit)? = null
    var itemClickListenerNextPage : ((Post) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    inner class PostViewHolder(private val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(post: Post) {
            binding.titleTextView.text = post.title
            binding.bodyTextView.text = post.body

            binding.root.setOnClickListener {
                itemClickListener?.invoke(post)
            }

            binding.nextIcon.setOnClickListener {
                itemClickListenerNextPage?.invoke(post)
            }
        }
    }
}

class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean = oldItem == newItem
}