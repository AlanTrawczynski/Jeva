package com.jeva.jeva.home

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.maps.model.LatLng
import com.jeva.jeva.R
import com.jeva.jeva.images.dataPointMenu

class HomeActivity : AppCompatActivity() {

    companion object{
        var lastMapZoom: Float = 14f
        var lastMapPosition: LatLng = LatLng(0.0,0.0)
    }

    val REQUEST_CODE: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(setOf(
            R.id.navigation_routes, R.id.navigation_dashboard, R.id.navigation_profile
        ))
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE) {
            val ref: Uri = data?.data!!
            dataPointMenu.aSubir.add(ref)
            dataPointMenu.adapter.add(ref) //returna una Uri, se la añado al adapter
        }
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }

}