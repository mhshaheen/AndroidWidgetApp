//package com.example.androidwidgetapp.recyclerViewPagination.fragment
//
//import android.os.Bundle
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.fragment.app.Fragment
//import androidx.fragment.app.viewModels
//import androidx.lifecycle.lifecycleScope
//import androidx.recyclerview.widget.LinearLayoutManager
//import com.example.androidwidgetapp.databinding.FragmentRoomOneBinding
//import com.example.androidwidgetapp.recyclerViewPagination.model.Post
//import com.example.androidwidgetapp.recyclerViewPagination.viewModel.PostViewModel
//import dagger.hilt.android.AndroidEntryPoint
//import kotlinx.coroutines.launch
//
//@AndroidEntryPoint
//class RoomFragmentOne : Fragment() {
//
//    private lateinit var binding: FragmentRoomOneBinding
//    private val viewModel: PostViewModel by viewModels()
//    lateinit var roomPostAdapter: RoomPostAdapter
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
//        binding = FragmentRoomOneBinding.inflate(layoutInflater, container, false)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        post = arguments?.getSerializable("key") as Post
//
//        init()
//        observer()
//        clickListener()
//    }
//
//    private fun init() {
//        roomPostAdapter = RoomPostAdapter()
//        binding.rV.layoutManager = LinearLayoutManager(requireActivity())
//        binding.rV.adapter = roomPostAdapter
//
//        viewModel.getPostsList()
//    }
//
//    private fun clickListener() {
//        roomPostAdapter.favItemClickListener = object : (Post) -> Unit {
//            override fun invoke(post: Post) {
//                Log.d("shaheen","fav item click: $post")
//                viewModel.toggleFavorite(post)
//            }
//        }
//    }
//
//    private fun observer() {
////        lifecycleScope.launch {
////            viewModel.postsList.collectLatest {
////                roomPostAdapter.submitData(it.toMutableList())
////            }
////        }
//
//        lifecycleScope.launch {
//            viewModel.allPosts.observe(viewLifecycleOwner) { posts ->
//                posts?.let {
//                    roomPostAdapter.submitData(it.toMutableList())
//                }
//            }
//        }
//    }
//
//    companion object {
//        @JvmStatic
//        fun newInstance(param1: String, param2: String) =
//            RoomFragmentOne().apply {
//                arguments = Bundle().apply {
//
//                }
//            }
//    }
//}