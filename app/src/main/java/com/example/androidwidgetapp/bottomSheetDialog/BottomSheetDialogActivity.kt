package com.example.androidwidgetapp.bottomSheetDialog

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.androidwidgetapp.R
import com.example.androidwidgetapp.databinding.ActivityBottomSheetDialogBinding
import com.example.androidwidgetapp.interfaces.GenericInterfaceListener
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class BottomSheetDialogActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBottomSheetDialogBinding
    //private lateinit var noInternetDialog: NoInternetBottomSheetFragmentDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBottomSheetDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        clickListener()
    }

    private fun clickListener() {
        binding.toastClick.setOnClickListener {
            showToast("Toast Click")
        }
        binding.bottomSheetClick.setOnClickListener {
            showBottomSheetDialog()
        }
        binding.alertDialogClick.setOnClickListener {
            //showDialog(this,"Alert Dialog","This is a Alert Dialog with Two Buttons in Android")
            //showAlertDialog(this)
            MaterialAlertDialogBuilder(this).apply {
                setTitle("Title")
                setMessage("Message")
                setPositiveButton("Ok"){ dialog, _ ->
                    dialog.dismiss()
                }
                setNegativeButton("Cancel"){ dialog, _ ->
                    dialog.dismiss()
                }
            }.create().show()
        }
        binding.snackBarClick.setOnClickListener {
            Snackbar.make(binding.snackBarClick, "My Message", Snackbar.LENGTH_LONG)
                .setAction("Ok") {
                    Toast.makeText(this, "Clicked", Toast.LENGTH_SHORT).show()
                }
                .show()
        }
    }

    private fun showAlertDialog(context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)
        builder.setMessage("Hello")

        builder.setPositiveButton("OK") { _, _ ->
            Toast.makeText(this,"You press OK button",Toast.LENGTH_SHORT).show()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
            Toast.makeText(this,"You press Cancel button",Toast.LENGTH_SHORT).show()
        }

        builder.setCancelable(false)
        val dialog = builder.create()

        dialog.show()
    }

    private fun showBottomSheetDialog() {
        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(R.layout.no_internet_bottomsheet_dialog)
        bottomSheetDialog.show()

        bottomSheetDialog.findViewById<TextView>(R.id.titleTv)?.setOnClickListener {
            showToast("Bottom sheet dialog click")
            bottomSheetDialog.dismiss()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(
            this,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun customToast(message: String) {
        val inflater = layoutInflater
        val layout: View = inflater.inflate(
            com.example.androidwidgetapp.R.layout.custom_toast_layout,
            findViewById<ViewGroup>(com.example.androidwidgetapp.R.id.rootToastLayout)
        )
        val text = layout.findViewById<TextView>(com.example.androidwidgetapp.R.id.messageTv)
        text.text = message
        val toast = Toast(applicationContext)
        layout.minimumWidth = resources.displayMetrics.widthPixels - 100
        toast.duration = Toast.LENGTH_LONG
        toast.setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 20)
        toast.view = layout
        toast.show()
    }
}