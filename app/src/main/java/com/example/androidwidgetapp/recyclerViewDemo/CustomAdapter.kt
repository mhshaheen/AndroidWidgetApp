package com.example.androidwidgetapp.recyclerViewDemo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.androidwidgetapp.databinding.ItemSubPlanBinding

class CustomAdapter : RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

    var itemClickListener : ((ItemsViewModel) -> Unit)? = null

    private var mList: MutableList<ItemsViewModel> = ArrayList()

    inner class ViewHolder(val binding : ItemSubPlanBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSubPlanBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ItemsViewModel = mList[position]
        with(holder){
            binding.planIconIV.setImageResource(ItemsViewModel.image)
            binding.planNameTV.text = ItemsViewModel.text

            binding.root.setOnClickListener {
                itemClickListener?.invoke(ItemsViewModel)
            }
        }
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    fun submitData(mList: MutableList<ItemsViewModel>) {
        this.mList = mList
        notifyDataSetChanged()
    }
}