package com.example.androidwidgetapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.androidwidgetapp.databinding.RvItemBinding
import com.example.androidwidgetapp.interfaces.GenericInterfaceListener

class RVAdapter : RecyclerView.Adapter<RVAdapter.ViewHolder>() {

    private var oldList = emptyList<String>()
    var listener: GenericInterfaceListener<String>? = null

    inner class ViewHolder(val binding: RvItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RVAdapter.ViewHolder {
        return ViewHolder(RvItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: RVAdapter.ViewHolder, position: Int) {
        with(holder){
            with(oldList[position]) {
                binding.root.setOnClickListener { listener?.clickListener(this) }
                binding.root.setOnLongClickListener {
                    listener?.longClickListener(this)
                    true
                }
                binding.tvName.text = this
                binding.tvAge.text = this
            }
        }
    }

    override fun getItemCount() = oldList.size

    fun setData(newList: List<String>) {
        val diffUtil = RVDiffUtil(oldList, newList)
        val diffResult = DiffUtil.calculateDiff(diffUtil)
        oldList = newList
        diffResult.dispatchUpdatesTo(this)
    }
}