package com.example.androidwidgetapp.coroutineScope

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.androidwidgetapp.databinding.ActivityCoroutineScopeBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class CoroutineScopeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCoroutineScopeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCoroutineScopeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.withContextBtn.setOnClickListener {
            lifecycleScope.launch {
                executeCoroutineWithContext()
            }
        }

        binding.runBlockBtn.setOnClickListener {
            lifecycleScope.launch {
                executeCoroutineRunBlocking()
            }
        }
    }

    // these two are Blocking nature
    private suspend fun executeCoroutineWithContext() {
        Log.d("checkCoroutine", "with context -> 1st")
        withContext(Dispatchers.IO) {
            delay(1000)
            Log.d("checkCoroutine", "with context -> 2nd")
        }
        Log.d("checkCoroutine", "with context -> 3rd")
    }

    private suspend fun executeCoroutineRunBlocking() {
        Log.d("checkCoroutine", "run blocking -> 1st")
        runBlocking(Dispatchers.IO) {
            delay(1000)
            Log.d("checkCoroutine", "run blocking -> 2nd")
        }
        Log.d("checkCoroutine", "run blocking -> 3rd")
    }
}