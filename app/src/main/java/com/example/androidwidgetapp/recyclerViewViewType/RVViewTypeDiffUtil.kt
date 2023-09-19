package com.example.androidwidgetapp.recyclerViewViewType

import androidx.recyclerview.widget.DiffUtil

class RVViewTypeDiffUtil(private var oldList: List<String>, private var newList: List<String>) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList != newList
    }
}