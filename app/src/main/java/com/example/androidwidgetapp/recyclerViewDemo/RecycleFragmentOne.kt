package com.example.androidwidgetapp.recyclerViewDemo

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.androidwidgetapp.R
import com.example.androidwidgetapp.databinding.FragmentRecycleOneBinding
import com.example.androidwidgetapp.media3Player.GenericTrack.Companion.safeJson

class RecycleFragmentOne : Fragment() {

    private lateinit var binding: FragmentRecycleOneBinding
    private lateinit var customAdapter: CustomAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRecycleOneBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
    }

    private fun init() {
        customAdapter = CustomAdapter()

        binding.rv.layoutManager = LinearLayoutManager(requireActivity())
        binding.rv.adapter = customAdapter

        customAdapter.submitData(
            arrayListOf(
                ItemsViewModel(R.drawable.ic_sub_plan, "Daily"),
                ItemsViewModel(R.drawable.ic_sub_plan, "Monthly"),
                ItemsViewModel(R.drawable.ic_sub_plan, "Half Yearly"),
                ItemsViewModel(R.drawable.ic_sub_plan, "Yearly")
            )
        )

        customAdapter.itemClickListener = object : (ItemsViewModel) -> Unit {
            override fun invoke(itemsViewModel: ItemsViewModel) {
                val bundle = bundleOf(
                    "key" to safeJson.encodeToString<ItemsViewModel>(itemsViewModel)
                )
                findNavController().navigate(R.id.action_recycleFragment_to_recycleFragmentTwo, bundle)
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = RecycleFragmentOne().apply {
            arguments = Bundle().apply {

            }
        }
    }
}