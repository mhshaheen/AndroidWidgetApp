package com.example.androidwidgetapp.recyclerViewViewType

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.androidwidgetapp.databinding.RvViewItemOneBinding
import com.example.androidwidgetapp.databinding.RvViewItemTwoBinding

class RVViewTypeAdapter(private val context: Context, private val list: ArrayList<DataSource>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    inner class ViewHolderOne(val bindingRVOne: RvViewItemOneBinding) :
        RecyclerView.ViewHolder(bindingRVOne.root) {
        fun bindRVOne(position: Int) {
            val recyclerViewModel = list[position]
            bindingRVOne.textView.text = recyclerViewModel.textData
        }
    }

    inner class ViewHolderTwo(val bindingRVTwo: RvViewItemTwoBinding) :
        RecyclerView.ViewHolder(bindingRVTwo.root) {
        fun bindRVTwo(position: Int) {
            val recyclerViewModel = list[position]
            bindingRVTwo.textView.text = recyclerViewModel.textData
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return if (viewType == VIEW_TYPE_ONE) {
            ViewHolderOne(
                RvViewItemOneBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        } else {
            ViewHolderTwo(
                RvViewItemTwoBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (list[position].viewType == VIEW_TYPE_ONE) {
            (holder as ViewHolderOne).bindRVOne(position)
        } else {
            (holder as ViewHolderTwo).bindRVTwo(position)
        }
    }

    override fun getItemCount(): Int = list.size

    override fun getItemViewType(position: Int): Int {
        return list[position].viewType
    }

    companion object {
        const val VIEW_TYPE_ONE = 1
        const val VIEW_TYPE_TWO = 2
    }
}