package com.jeva.jeva.home

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.ui.IconGenerator
import com.jeva.jeva.R
import com.jeva.jeva.images.dataPointMenu
import java.util.*
import kotlin.collections.HashMap

class EditRoute : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var nMap: GoogleMap
    private lateinit var routeData : HashMap<String, Any>
    private lateinit var iconGenerator: IconGenerator
    private var newRoute: Boolean = false

    private var initialZoom: Float = 14f
    private lateinit var initialPosition: LatLng

    private var markerIndex = 0
    private var markerList = mutableListOf<Marker>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_edit_route)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


        newRoute = intent.getBooleanExtra("newRoute", false)
        Log.i("Maps", "Esto es una nueva ruta?: $newRoute")
        if(!newRoute){
            Log.i("Maps", "He entrado debería de estar cargando todas las mierdas")
            initialZoom = intent.getFloatExtra("mapZoom", 14f)
            routeData = intent.getSerializableExtra("routeData") as HashMap<String, Any>
            initialPosition = positionToLatLng(routeData["position"] as HashMap<String,Any>)
            Log.i("Maps", "Esta es mi ruta: $routeData")

        }
        else{
            initialPosition = HomeActivity.lastMapPosition
            initialZoom = HomeActivity.lastMapZoom
        }

        this.title = "Editar"
    }


    override fun onMapReady(googleMap: GoogleMap) {
        nMap = googleMap
        iconGenerator = IconGenerator(this)
        if(!newRoute){
            Log.i("Maps", "He entrado, creame la nueva ruta")
            showRoute()
        }
        nMap.apply {
            moveCamera(CameraUpdateFactory.newLatLngZoom(initialPosition, initialZoom))
        }

        nMap.setOnMarkerClickListener {
                marker ->
            val tag = marker.tag as Map<*, *>
            val title: String = tag["title"] as String
            val description: String = tag["description"] as String
            val idMarker = tag["id"] as String
            val idRoute = routeData["id"] as String

            dataPointMenu.setInfo(title,description, idRoute, idMarker, this, this.applicationContext,this.layoutInflater)
            dataPointMenu.showMenu(null,true)
            true
        }

        nMap.setOnMapClickListener { latLng ->
            markerIndex += 1
            val marker = nMap.addMarker(MarkerOptions().position(latLng).title("Hola $markerIndex"))
            marker?.let{
                marker.tag = mutableMapOf(
                    "id" to UUID.randomUUID().toString(),
                    "index" to markerIndex,
                    "title" to "",
                    "description" to ""
                )
                marker.setIcon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon((marker.tag as MutableMap<String, Any>)["index"].toString())))
                markerList.add(marker)
            }
        }

    }

    private fun showRoute(){
        Log.i("Maps", routeData.toString())
        for((i,marker0) in (routeData["markers"] as List<*>).withIndex()){
            val latLng: LatLng = positionToLatLng(marker0 as Map<String, Any>)
            val marker = nMap.addMarker(MarkerOptions().position(latLng))

            if (i == 0) { iconGenerator.setStyle(IconGenerator.STYLE_PURPLE) }
            else { iconGenerator.setStyle(IconGenerator.STYLE_BLUE) }
            marker?.let {
                marker.tag = marker0["tag"]
                marker.setIcon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon()))
                markerList.add(marker)
            }
        }
        markerIndex = markerList.size
        Log.i("Maps", "El maximo index en la lista es: $markerIndex")
    }

    private fun positionToLatLng(position: Map<String, Any>) : LatLng {
        return LatLng(position["lat"] as Double, position["lng"]  as Double)
    }
}