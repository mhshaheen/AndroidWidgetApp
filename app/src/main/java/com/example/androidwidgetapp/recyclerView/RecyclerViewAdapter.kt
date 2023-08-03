package com.example.androidwidgetapp.recyclerView

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DiffUtil.DiffResult
import androidx.recyclerview.widget.RecyclerView
import com.example.androidwidgetapp.databinding.RvItemBinding

class RecyclerViewAdapter : RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {

    private var oldList = emptyList<String>()

    inner class ViewHolder(val binding: RvItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(RvItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount() = oldList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder){
            with(oldList[position]) {
                binding.tvName.text = this
                binding.tvAge.text = this
            }
        }
    }

    fun setData(newList: List<String>) {
        val diffUtil = RecyclerViewDiffUtil(oldList, newList)
        val diffResult = DiffUtil.calculateDiff(diffUtil)
        oldList = newList
        diffResult.dispatchUpdatesTo(this)
    }

}