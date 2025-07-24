package com.example.androidwidgetapp.module

import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import com.example.androidwidgetapp.R
import com.example.androidwidgetapp.databinding.ActivityModuleBinding
import com.google.android.material.snackbar.Snackbar

class ModuleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityModuleBinding
    private lateinit var snackbar: Snackbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityModuleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState != null) return

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            openTestFragment()
            return
        }

        var permissions = arrayOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !getSystemService(NotificationManager::class.java).areNotificationsEnabled()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissions = permissions.plus(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            snackbar =
                Snackbar.make(binding.root, "Allow storage permission", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Settings") {
                        val getpermission = Intent()
                        getpermission.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                        startActivity(getpermission)
                        snackbar.dismiss()
                    }
            snackbar.show()
        } else if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                permissions = permissions.plus(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }

        if (permissions.isNotEmpty()) {
            requestPermissions(permissions, 101)
            Toast.makeText(this, "Notification and Storage Permission Required", Toast.LENGTH_SHORT)
                .show()
            return
        }

        openTestFragment()
    }

    private fun openTestFragment() {
        //Test with your own url
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, ModuleFragment.newInstance()).commit()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 101) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openTestFragment()
            }
        }
    }
}