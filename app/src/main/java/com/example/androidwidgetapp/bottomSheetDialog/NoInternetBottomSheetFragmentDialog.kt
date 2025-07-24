package com.example.androidwidgetapp.bottomSheetDialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.androidwidgetapp.databinding.NoInternetBottomsheetDialogBinding
import com.example.androidwidgetapp.interfaces.GenericInterfaceListener
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class NoInternetBottomSheetFragmentDialog : BottomSheetDialogFragment() {

    private lateinit var binding: NoInternetBottomsheetDialogBinding
    var listener: GenericInterfaceListener<String>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = NoInternetBottomsheetDialogBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        clickListener()
    }

    private fun clickListener() {
        binding.titleTv.setOnClickListener {
            listener?.clickListener("Hello")
        }
    }
}