//package com.example.androidwidgetapp.recyclerViewPagination.fragment
//
//import android.os.Bundle
//import androidx.fragment.app.Fragment
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import com.example.androidwidgetapp.databinding.FragmentPaginationTwoBinding
//import com.example.androidwidgetapp.recyclerViewPagination.model.Post
//import dagger.hilt.android.AndroidEntryPoint
//
//@AndroidEntryPoint
//class PaginationFragmentTwo : Fragment() {
//
//    private lateinit var binding: FragmentPaginationTwoBinding
//    private var post: Post? = null
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        arguments?.let {
//
//        }
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        binding = FragmentPaginationTwoBinding.inflate(layoutInflater, container, false)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        post = arguments?.getSerializable("key") as Post
//
//        init()
//    }
//
//    private fun init() {
//        post?.let {
//            binding.paginationTwo.text = it.title
//        }
//    }
//
//    companion object {
//        @JvmStatic
//        fun newInstance(param1: String, param2: String) =
//            PaginationFragmentTwo().apply {
//                arguments = Bundle().apply {
//
//                }
//            }
//    }
//}