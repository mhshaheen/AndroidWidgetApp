package com.example.androidwidgetapp.tabLayout

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.viewpager2.widget.ViewPager2
import com.example.androidwidgetapp.R
import com.example.androidwidgetapp.databinding.ActivityTabLayoutBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class TabLayoutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTabLayoutBinding

    var tabTitle = arrayOf("EntryEffect", "Crown", "Backpack")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTabLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.viewPager2.adapter = MyAdapter(supportFragmentManager, lifecycle)

        TabLayoutMediator(binding.tabLayout, binding.viewPager2) { tab, position ->
            tab.text = tabTitle[position]
        }.attach()


        // For Tab layout selected listener

//        val images = listOf(
//            R.drawable.bd,
//            R.drawable.america,
//            R.drawable.canada,
//            R.drawable.germany,
//        )
//
//        val adapter = ViewPagerAdapter(images)
//        binding.viewPager.adapter = adapter
//
//        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
//            tab.text = "Tab ${position+1}"
//        }.attach()
//
//        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
//            override fun onTabSelected(tab: TabLayout.Tab?) {
//                Toast.makeText(this@TabLayoutActivity, "Selected ${tab?.text}", Toast.LENGTH_SHORT).show()
//            }
//
//            override fun onTabUnselected(tab: TabLayout.Tab?) {
//
//            }
//
//            override fun onTabReselected(tab: TabLayout.Tab?) {
//
//            }
//        })
//
//        binding.viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
//
//        binding.viewPager.beginFakeDrag()
//        binding.viewPager.fakeDragBy(-10f)
//        binding.viewPager.endFakeDrag()
    }
}