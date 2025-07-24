package com.example.androidwidgetapp.recyclerViewPagination.fragment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.androidwidgetapp.R
import com.example.androidwidgetapp.databinding.ItemFavPostBinding
import com.example.androidwidgetapp.databinding.ItemPostBinding
import com.example.androidwidgetapp.databinding.ItemSubPlanBinding
import com.example.androidwidgetapp.recyclerViewDemo.ItemsViewModel
import com.example.androidwidgetapp.recyclerViewPagination.model.Post

class RoomPostAdapter : RecyclerView.Adapter<RoomPostAdapter.ViewHolder>() {

    var favItemClickListener : ((Post) -> Unit)? = null

    private var mList: MutableList<Post> = ArrayList()

    inner class ViewHolder(val binding : ItemFavPostBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFavPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val postItem = mList[position]
        with(holder){
            binding.titleTextView.text = postItem.title
            binding.bodyTextView.text = postItem.body

            binding.root.setOnClickListener {
                //itemClickListener?.invoke(postItem)
            }

            binding.favIcon.setImageResource(
                if (postItem.isFav) R.drawable.baseline_favorite_24 else R.drawable.baseline_favorite_border_24
            )

            binding.favIcon.setOnClickListener {
                favItemClickListener?.invoke(postItem)

                postItem.isFav = !postItem.isFav
                binding.favIcon.setImageResource(
                    if (postItem.isFav) R.drawable.baseline_favorite_24 else R.drawable.baseline_favorite_border_24
                )
            }
        }
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    fun submitData(mList: MutableList<Post>) {
        this.mList = mList
        notifyDataSetChanged()
    }
}