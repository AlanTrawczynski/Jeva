package com.jeva.jeva

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.draw_route.*

class DrawRoute : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.draw_route)
        val routeData : HashMap<String, Any> = intent.getSerializableExtra("routeData") as HashMap<String, Any>
        drawRouteTextView.text = routeData.toString()
    }

}