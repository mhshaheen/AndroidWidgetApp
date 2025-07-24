package com.example.androidwidgetapp.recyclerViewPagination

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.androidwidgetapp.R
import com.example.androidwidgetapp.databinding.ActivityRvpaginationBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RVPaginationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRvpaginationBinding
    private lateinit var navController: NavController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRvpaginationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerOne) as NavHostFragment
        navController = navHostFragment.navController

        val navGraph = navController.navInflater.inflate(R.navigation.nav_graph_sample)
        navController.graph = navGraph
    }
}