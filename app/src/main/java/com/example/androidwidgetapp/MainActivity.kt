package com.example.androidwidgetapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.androidwidgetapp.databinding.ActivityMainBinding
import com.example.androidwidgetapp.interfaces.GenericInterfaceListener
import com.example.androidwidgetapp.recyclerView.RecyclerViewActivity
import java.util.Objects

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val rvAdapter by lazy { RVAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
    }

    private fun init(){
        binding.rvList.layoutManager = LinearLayoutManager(this)
        binding.rvList.adapter = rvAdapter

        val itemList = listOf(
            "Recycler View",
            "Recycler View 2",
            "Recycler View 3",
            "Recycler View 4",
            "Recycler View 5",
            "Recycler View 6",
            "Recycler View 7",
            "Recycler View 8",
            "Recycler View 9",
            "Recycler View 10",
            "Recycler View 11",
            "Recycler View 12",
            "Recycler View 13",
            "Recycler View 14",
            "Recycler View 15",
        )
        rvAdapter.setData(itemList)

        rvAdapter.listener = object : GenericInterfaceListener<String> {

            override fun clickListener(any: String) {
                goToDestination(any)
            }

            override fun longClickListener(any: String) {
                Toast.makeText(applicationContext, "This is RecyclerViewActivity", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun goToDestination(any: String) {
        when(any){
            "Recycler View" -> { startActivity(Intent(this@MainActivity, RecyclerViewActivity::class.java)) }
        }
    }
}