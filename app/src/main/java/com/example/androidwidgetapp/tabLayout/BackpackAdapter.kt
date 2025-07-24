package com.example.androidwidgetapp.tabLayout

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.androidwidgetapp.databinding.ItemViewBackpackBinding

class BackpackAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val dataList: MutableList<String> = mutableListOf()
    var onItemClick: ((position: Int, model: String) -> Unit)? = null
    var onItemInstantClick: ((position: Int, model: String) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding: ItemViewBackpackBinding = ItemViewBackpackBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewModel(binding)
    }

    override fun getItemCount(): Int = dataList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewModel) {
            val model = dataList[position]
            val binding = holder.binding

        }
    }

    internal inner class ViewModel(val binding: ItemViewBackpackBinding): RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    //onItemClick?.invoke(absoluteAdapterPosition, dataList[absoluteAdapterPosition])
                }
            }
        }
    }

    fun initLoad(list: List<String>) {
        dataList.clear()
        notifyDataSetChanged()
    }
}