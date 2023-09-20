package com.example.androidwidgetapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.androidwidgetapp.ads.AdActivity
import com.example.androidwidgetapp.databinding.ActivityMainBinding
import com.example.androidwidgetapp.googleBilling.GoogleBillingActivity
import com.example.androidwidgetapp.interfaces.GenericInterfaceListener
import com.example.androidwidgetapp.recyclerView.RecyclerViewActivity
import com.example.androidwidgetapp.recyclerViewViewType.RVViewTypeActivity
import com.example.androidwidgetapp.recyclerViewViewType.stickyHeaderType.StickHeaderActivity

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
            "Google Billing",
            "Ad",
            "Recycler View View Type",
            "Recycler View Sticky Header",
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
            "Google Billing" -> { startActivity(Intent(this@MainActivity, GoogleBillingActivity::class.java)) }
            "Ad" -> { startActivity(Intent(this@MainActivity, AdActivity::class.java)) }
            "Recycler View View Type" -> { startActivity(Intent(this@MainActivity, RVViewTypeActivity::class.java)) }
            "Recycler View Sticky Header" -> { startActivity(Intent(this@MainActivity, StickHeaderActivity::class.java)) }
        }
    }
}