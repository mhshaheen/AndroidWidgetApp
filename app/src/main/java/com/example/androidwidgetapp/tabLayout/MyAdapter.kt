package com.example.androidwidgetapp.tabLayout

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.androidwidgetapp.tabLayout.tabFragment.BackpackFragment
import com.example.androidwidgetapp.tabLayout.tabFragment.CrownFragment
import com.example.androidwidgetapp.tabLayout.tabFragment.EntryEffectFragment

class MyAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) : FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> EntryEffectFragment()
            1 -> CrownFragment()
            2 -> BackpackFragment()
            else -> EntryEffectFragment()
        }
    }
}