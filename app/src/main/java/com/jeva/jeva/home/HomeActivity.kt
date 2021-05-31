package com.jeva.jeva.home

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.jeva.jeva.GestionarPermisos
import com.jeva.jeva.ObtencionLocalizacion
import com.jeva.jeva.R
import com.jeva.jeva.images.dataPointMenu

class HomeActivity : AppCompatActivity() {

    companion object {
        var lastMapZoom: Float = 4F
        var lastMapPosition: LatLng = LatLng(0.0,0.0)
    }

    private val obtencionLocalizacion = ObtencionLocalizacion()
    private val REQUEST_CODE: Int = 1



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(setOf(
            R.id.navigation_explore, R.id.navigation_myroutes
        ))
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        Log.i("Pruebas", "He entrado en HomeActivity")

        saveMyLocation()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE) {
            val ref: Uri = data?.data!!
            dataPointMenu.uploadImageShow(ref)
        }
    }


    private fun saveMyLocation() {
        Log.i("Pruebas", "He entrado ")
        GestionarPermisos.requestLocationPermissions(this)
        obtencionLocalizacion.localizacion(this)
            .addOnSuccessListener { location ->
                location?.let { loc ->
                    Log.i("Pruebas", "He entrado en saveMyLocation")
                    lastMapPosition = LatLng(loc.latitude, loc.longitude)
                }
            }
            .addOnCanceledListener {
                Log.i("Pruebas", "No he entrado en saveMyLocation")
            }
    }

}