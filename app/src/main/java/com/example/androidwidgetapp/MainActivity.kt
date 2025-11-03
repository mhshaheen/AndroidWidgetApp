package com.example.androidwidgetapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.androidwidgetapp.ads.AdActivity
import com.example.androidwidgetapp.bottomSheetDialog.BottomSheetDialogActivity
import com.example.androidwidgetapp.coroutineScope.CoroutineScopeActivity
import com.example.androidwidgetapp.customMedia3PlayerImplementationDemo.MusicPlayerActivity
import com.example.androidwidgetapp.databinding.ActivityMainBinding
import com.example.androidwidgetapp.googleBilling.GoogleBillingActivity
import com.example.androidwidgetapp.interfaces.GenericInterfaceListener
import com.example.androidwidgetapp.media3Player.Media3PlayerActivity
import com.example.androidwidgetapp.module.ModuleActivity
import com.example.androidwidgetapp.recyclerView.RecyclerViewActivity
import com.example.androidwidgetapp.recyclerViewDemo.RecyclerViewDemoActivity
import com.example.androidwidgetapp.recyclerViewPagination.RVPaginationActivity
import com.example.androidwidgetapp.recyclerViewViewType.RVViewTypeActivity
import com.example.androidwidgetapp.recyclerViewViewType.stickyHeaderType.StickHeaderActivity
import com.example.androidwidgetapp.shortsPlayer.ShortsPlayerActivity
import com.example.androidwidgetapp.storyView.StoryActivity
import com.example.androidwidgetapp.tabLayout.TabLayoutActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val rvAdapter by lazy { RVAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
            "Coroutine Scope",
            "Tab layout",
            "Dialogs",
            //"Facebook Story",
            "Recycler View Demo",
            "Module",
            //"Recycler View Pagination",
            "Shorts Player",
            "Media3 Player",
            "Custom Media3 Player",
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
            // todo 0 task -> basic recycler view
            // todo 1 task -> for various types recycler view
            "Recycler View" -> { startActivity(Intent(this@MainActivity, RecyclerViewActivity::class.java)) }
            "Google Billing" -> { startActivity(Intent(this@MainActivity, GoogleBillingActivity::class.java)) }
            "Ad" -> { startActivity(Intent(this@MainActivity, AdActivity::class.java)) }
            "Recycler View View Type" -> { startActivity(Intent(this@MainActivity, RVViewTypeActivity::class.java)) }
            "Recycler View Sticky Header" -> { startActivity(Intent(this@MainActivity, StickHeaderActivity::class.java)) }
            "Coroutine Scope" -> { startActivity(Intent(this@MainActivity, CoroutineScopeActivity::class.java)) }
            "Tab layout" -> { startActivity(Intent(this@MainActivity, TabLayoutActivity::class.java)) }
            "Dialogs" -> { startActivity(Intent(this@MainActivity, BottomSheetDialogActivity::class.java)) }
            //"Facebook Story" -> { startActivity(Intent(this@MainActivity, StoryActivity::class.java)) }
            // todo 2 task -> using nav fragments recycler view
            "Recycler View Demo" -> { startActivity(Intent(this@MainActivity, RecyclerViewDemoActivity::class.java)) }
            "Module" -> {startActivity(Intent(this@MainActivity, ModuleActivity::class.java))}
            "Shorts Player" -> {startActivity(Intent(this@MainActivity, ShortsPlayerActivity::class.java))}
            "Media3 Player" -> {startActivity(Intent(this@MainActivity, Media3PlayerActivity::class.java))}
            "Custom Media3 Player" -> {startActivity(Intent(this@MainActivity, MusicPlayerActivity::class.java))}
            //"Recycler View Pagination" -> { startActivity(Intent(this@MainActivity, RVPaginationActivity::class.java)) }
        }
    }
}