package com.example.androidwidgetapp

import android.annotation.SuppressLint
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
        val item = oldList[position]
        holder.binding.apply {
            root.setOnClickListener { listener?.clickListener(item) }
            root.setOnLongClickListener {
                listener?.longClickListener(item)
                true
            }
            tvName.text = item
            tvAge.text = item
        }
    }

    override fun getItemCount() = oldList.size

    @SuppressLint("NotifyDataSetChanged")
    fun setData(newList: List<String>) {
        oldList = newList
        notifyDataSetChanged()

//        val diffUtil = RVDiffUtil(oldList, newList)
//        val diffResult = DiffUtil.calculateDiff(diffUtil)
//        oldList = newList
//        diffResult.dispatchUpdatesTo(this)
    }
}