package com.example.androidwidgetapp.recyclerView

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.androidwidgetapp.R
import com.example.androidwidgetapp.databinding.ActivityRecyclerViewBinding

class RecyclerViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecyclerViewBinding

    private val recyclerViewAdapter by lazy { RecyclerViewAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecyclerViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
    }

    private fun init() {
        layoutTransform()

        val itemList = listOf(
            "Item 1",
            "Item 2",
            "Item 3",
            "Item 4",
            "Item 5",
            "Item 6",
            "Item 7",
            "Item 8",
            "Item 9",
            "Item 10",
            "Item 11",
            "Item 12",
            "Item 13 stagger example",
            "Item 14",
            "Item 15",
            "Item 16",
            "Item 17",
            "Item 18 stagger example stagger example",
            "Item 19",
            "Item 20",
            "Item 21",
            "Item 22",
            "Item 23 stagger example stagger example stagger example",
            "Item 24",
            "Item 25",
            "Item 26",
        )

        recyclerViewAdapter.setData(itemList)
    }

    private fun layoutTransform() {
        binding.rvList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rvList.adapter = recyclerViewAdapter

        binding.verticalBtn.setOnClickListener {
            binding.rvList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
            binding.rvList.adapter = recyclerViewAdapter
        }

        binding.horizontalBtn.setOnClickListener {
            binding.rvList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            binding.rvList.adapter = recyclerViewAdapter
        }

        binding.gridBtn.setOnClickListener {
            binding.rvList.layoutManager = GridLayoutManager(this, 2)
            binding.rvList.adapter = recyclerViewAdapter
        }

        binding.straggerBtn.setOnClickListener {
            binding.rvList.layoutManager = StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
            binding.rvList.adapter = recyclerViewAdapter
        }
    }
}