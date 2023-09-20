package com.example.androidwidgetapp.recyclerViewViewType.stickyHeaderType

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.androidwidgetapp.databinding.ActivityStickyHeaderBinding

class StickHeaderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStickyHeaderBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStickyHeaderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = UserAdapter(createDummyObjects())
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.addItemDecoration(StickyHeaderItemDecoration(adapter))
    }

    fun createDummyObjects(): List<User> {
        val dummyObjects = ArrayList<User>()
        for (i in 1..5) {  //Repeating to just create more objects to show the scroll.
            dummyObjects += User("Aaa", -1, true)
            dummyObjects += User("Aaa", -1, false)
            dummyObjects += User("Aaa", -1, false)
            dummyObjects += User("Aaa", -1, false)
            dummyObjects += User("Aaa", -1, false)
            dummyObjects += User("Aaa", -1, false)
            dummyObjects += User("Aaa", -1, false)
            dummyObjects += User("Baa", -1, true)
            dummyObjects += User("Baa", -1, false)
            dummyObjects += User("Baa", -1, false)
            dummyObjects += User("Baa", -1, false)
            dummyObjects += User("Baa", -1, false)
            dummyObjects += User("Baa", -1, false)
            dummyObjects += User("Baa", -1, false)
            dummyObjects += User("Baa", -1, false)
            dummyObjects += User("Caa", -1, true)
            dummyObjects += User("Caa", -1, false)
            dummyObjects += User("Caa", -1, false)
            dummyObjects += User("Caa", -1, false)
            dummyObjects += User("Caa", -1, false)
            dummyObjects += User("Caa", -1, false)
            dummyObjects += User("Caa", -1, false)
            dummyObjects += User("Caa", -1, false)
            dummyObjects += User("Caa", -1, false)
            dummyObjects += User("Caa", -1, false)
            dummyObjects += User("Caa", -1, false)
            dummyObjects += User("Caa", -1, false)
            dummyObjects += User("Caa", -1, false)
            dummyObjects += User("Caa", -1, false)
            dummyObjects += User("Caa", -1, false)
            dummyObjects += User("Daa", -1, true)
            dummyObjects += User("Daa", -1, false)
            dummyObjects += User("Daa", -1, false)
            dummyObjects += User("Daa", -1, false)
            dummyObjects += User("Daa", -1, false)
            dummyObjects += User("Daa", -1, false)
            dummyObjects += User("Daa", -1, false)
            dummyObjects += User("Daa", -1, false)
            dummyObjects += User("Daa", -1, false)
        }
        return dummyObjects
    }

}