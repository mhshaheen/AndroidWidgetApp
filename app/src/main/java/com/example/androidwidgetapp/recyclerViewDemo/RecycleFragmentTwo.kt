package com.example.androidwidgetapp.recyclerViewDemo

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.androidwidgetapp.databinding.FragmentRecycleTwoBinding
import com.example.androidwidgetapp.media3Player.GenericTrack.Companion.safeJson

class RecycleFragmentTwo : Fragment() {

    private lateinit var binding: FragmentRecycleTwoBinding
    private var itemsViewModel: ItemsViewModel?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRecycleTwoBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            itemsViewModel = safeJson.decodeFromString<ItemsViewModel>(it.getString("key")?:"")
        }

        init()
    }

    private fun init() {
        itemsViewModel?.let {
            binding.topTV.text = it.text
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            RecycleFragmentTwo().apply {
                arguments = Bundle().apply {

                }
            }
    }
}