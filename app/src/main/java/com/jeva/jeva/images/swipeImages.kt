package com.jeva.jeva.images

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.jeva.jeva.R
import com.jeva.jeva.images.adapters.swipeAdapter
import kotlinx.android.synthetic.main.activity_swipe_images.*

class swipeImages : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val supportActionBar = this.supportActionBar
        supportActionBar?.title = intent.getStringExtra("title")
        setContentView(R.layout.activity_swipe_images)

        var pos : Int = intent.getIntExtra("pos",0)
        var edit: Boolean = intent.getBooleanExtra("edit", false)

        var source = dataPointMenu.fotos.map { par -> par.second }.toCollection(ArrayList())

        if (edit) {
            source = ArrayList(source.slice(IntRange(0,source.size-2)))
        }

        var adapter: swipeAdapter = swipeAdapter(this.applicationContext, source)
        viewSwipe.adapter = adapter
        viewSwipe.currentItem = pos
    }
}