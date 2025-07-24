//package com.example.androidwidgetapp.recyclerViewPagination.fragment
//
//import android.os.Bundle
//import android.util.Log
//import androidx.fragment.app.Fragment
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.fragment.app.viewModels
//import androidx.lifecycle.lifecycleScope
//import androidx.navigation.fragment.findNavController
//import androidx.paging.LoadState
//import androidx.recyclerview.widget.LinearLayoutManager
//import com.example.androidwidgetapp.R
//import com.example.androidwidgetapp.databinding.FragmentPaginationOneBinding
//import com.example.androidwidgetapp.recyclerViewPagination.model.Post
//import com.example.androidwidgetapp.recyclerViewPagination.viewModel.PostViewModel
//import dagger.hilt.android.AndroidEntryPoint
//import kotlinx.coroutines.flow.collectLatest
//import kotlinx.coroutines.launch
//
//@AndroidEntryPoint
//class PaginationFragmentOne : Fragment() {
//
//    private lateinit var binding: FragmentPaginationOneBinding
//    private val viewModel: PostViewModel by viewModels()
//    private lateinit var postAdapter: PostAdapter
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
//        binding = FragmentPaginationOneBinding.inflate(layoutInflater, container, false)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        init()
//        clickListener()
//        observer()
//    }
//
//    private fun init() {
//        postAdapter = PostAdapter()
//        binding.rV.layoutManager = LinearLayoutManager(requireActivity())
//        binding.rV.adapter = postAdapter
//    }
//
//    private fun clickListener() {
//        postAdapter.itemClickListener = object : (Post) -> Unit {
//            override fun invoke(post: Post) {
//                val bundle = Bundle().apply {
//                    putSerializable("key", post)
//                }
//                findNavController().navigate(R.id.action_paginationFragmentOne_to_paginationFragmentTwo, bundle)
//            }
//        }
//
//        postAdapter.itemClickListenerNextPage = object : (Post) -> Unit {
//            override fun invoke(post: Post) {
//                val bundle = Bundle().apply {
//                    putSerializable("key", post)
//                }
//                findNavController().navigate(R.id.action_paginationFragmentOne_to_roomFragmentOne, bundle)
//            }
//        }
//    }
//
//    private fun observer() {
//        viewModel.isLoading.observe(viewLifecycleOwner) {
//            if (it) {
//                binding.loadingProgressBar.visibility = View.VISIBLE
//            } else {
//                binding.loadingProgressBar.visibility = View.GONE
//            }
//        }
//
//        lifecycleScope.launch {
//            viewModel.posts.collectLatest { pagingData ->
//                viewModel.setLoading(true)
//                Log.d("shaheen", "Submitting PagingData to Adapter $pagingData")
//                postAdapter.submitData(pagingData)
//                //viewModel.setLoading(true)
//            }
//
//            // todo using live data
////            viewModel.posts.observe(viewLifecycleOwner) { pagingData ->
////                Log.d("shaheen", "Submitting PagingData to Adapter")
////                viewModel.setLoading(true)
////                postAdapter.submitData(lifecycle, pagingData)
////                viewModel.setLoading(false)
////            }
//        }
//
//        // todo for showing loading state when using collect latest
//        lifecycleScope.launch {
//            postAdapter.loadStateFlow.collectLatest { loadStates ->
//                viewModel.setLoading(loadStates.source.refresh is LoadState.Loading)
//
//                // todo for scrolling data......
//                binding.loadingProgressBar.visibility = if (loadStates.append is LoadState.Loading) View.VISIBLE else View.GONE
//            }
//        }
//    }
//
//    companion object {
//
//        @JvmStatic
//        fun newInstance(param1: String, param2: String) =
//            PaginationFragmentOne().apply {
//                arguments = Bundle().apply {
//
//                }
//            }
//    }
//}