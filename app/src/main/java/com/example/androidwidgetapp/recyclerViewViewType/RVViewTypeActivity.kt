package com.example.androidwidgetapp.recyclerViewViewType

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.androidwidgetapp.databinding.ActivityRvviewTypeBinding

class RVViewTypeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRvviewTypeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRvviewTypeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
    }

    private fun init() {
        val dataList = ArrayList<DataSource>()

        dataList.add(DataSource(RVViewTypeAdapter.VIEW_TYPE_ONE, "1. Hi! I am in View 1"))
        dataList.add(DataSource(RVViewTypeAdapter.VIEW_TYPE_TWO, "2. Hi! I am in View 2"))
        dataList.add(DataSource(RVViewTypeAdapter.VIEW_TYPE_ONE, "3. Hi! I am in View 3"))
        dataList.add(DataSource(RVViewTypeAdapter.VIEW_TYPE_TWO, "4. Hi! I am in View 4"))
        dataList.add(DataSource(RVViewTypeAdapter.VIEW_TYPE_ONE, "5. Hi! I am in View 5"))
        dataList.add(DataSource(RVViewTypeAdapter.VIEW_TYPE_TWO, "6. Hi! I am in View 6"))
        dataList.add(DataSource(RVViewTypeAdapter.VIEW_TYPE_ONE, "7. Hi! I am in View 7"))
        dataList.add(DataSource(RVViewTypeAdapter.VIEW_TYPE_TWO, "8. Hi! I am in View 8"))
        dataList.add(DataSource(RVViewTypeAdapter.VIEW_TYPE_ONE, "9. Hi! I am in View 9"))
        dataList.add(DataSource(RVViewTypeAdapter.VIEW_TYPE_TWO, "10. Hi! I am in View 10"))
        dataList.add(DataSource(RVViewTypeAdapter.VIEW_TYPE_ONE, "11. Hi! I am in View 11"))
        dataList.add(DataSource(RVViewTypeAdapter.VIEW_TYPE_TWO, "12. Hi! I am in View 12"))
        dataList.add(DataSource(RVViewTypeAdapter.VIEW_TYPE_ONE, "13. Hi! I am in View 13"))
        dataList.add(DataSource(RVViewTypeAdapter.VIEW_TYPE_TWO, "14. Hi! I am in View 14"))
        dataList.add(DataSource(RVViewTypeAdapter.VIEW_TYPE_ONE, "15. Hi! I am in View 15"))
        dataList.add(DataSource(RVViewTypeAdapter.VIEW_TYPE_TWO, "16. Hi! I am in View 16"))
        dataList.add(DataSource(RVViewTypeAdapter.VIEW_TYPE_ONE, "17. Hi! I am in View 17"))
        dataList.add(DataSource(RVViewTypeAdapter.VIEW_TYPE_TWO, "18. Hi! I am in View 18"))
        dataList.add(DataSource(RVViewTypeAdapter.VIEW_TYPE_ONE, "19. Hi! I am in View 19"))
        dataList.add(DataSource(RVViewTypeAdapter.VIEW_TYPE_TWO, "20. Hi! I am in View 20"))
        dataList.add(DataSource(RVViewTypeAdapter.VIEW_TYPE_ONE, "21. Hi! I am in View 21"))

        val adapter = RVViewTypeAdapter(this, dataList)
        binding.rvList.layoutManager = LinearLayoutManager(this)
        binding.rvList.adapter = adapter


//        binding.rvList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
//        binding.rvList.adapter = rvViewTypeAdapter

        val itemList = listOf(
            "Item 1",
            "Item 2",
            "Item 3",
            "Item 4",
            "Item 5",
            "Item 6",
            "Item 7",
            "Item 8",
            "Item 9",
            "Item 10",
            "Item 11",
            "Item 12",
            "Item 13",
            "Item 14",
            "Item 15",
            "Item 16",
            "Item 17",
            "Item 18",
            "Item 19",
            "Item 20"
        )

        //rvViewTypeAdapter.setData(itemList)
    }
}