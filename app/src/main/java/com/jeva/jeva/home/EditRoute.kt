package com.jeva.jeva.home

import android.app.Activity
import android.content.Intent
import android.net.Uri
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
import com.jeva.jeva.Database
import com.jeva.jeva.R
import com.jeva.jeva.images.dataPointMenu
import com.jeva.jeva.images.routesPopUp
import kotlinx.android.synthetic.main.activity_edit_route.*
import java.util.*
import kotlin.collections.HashMap

class EditRoute : AppCompatActivity(), OnMapReadyCallback {

    companion object{
        lateinit var currentMarker: Marker
        private val db = Database()
        private var markerList = mutableListOf<Marker>()
        private lateinit var idRoute: String

        //private var markerIndex = 0

        fun deleteMarker(){
            val copyMarkerList = mutableListOf<Marker>()
            copyMarkerList.addAll(markerList)
            Log.i("Maps", "todo ok y el marcador es: $currentMarker")
            Log.i("Maps", "La lista es: $markerList")
            copyMarkerList.remove(currentMarker)
            db.updateRoute(idRoute, copyMarkerList){
                if(!it){
                    Log.i("Maps", "ERROR")
                }
                else{
                    markerList.remove(currentMarker)
                    currentMarker.remove()
                    Log.i("Maps", "Todo ok")
                }
            }
        }

        // subir nuevos puntos a la bd hecho

        // actualizar los datos de los puntos

        fun updateMarkersRoute(desc: String? = null, tit: String? = null){
            val copyMarkerList = mutableListOf<Marker>()
            copyMarkerList.addAll(markerList)
            val tag = mutableMapOf<String, Any>()
            tag.putAll(currentMarker.tag as MutableMap<String, Any>)
            desc.let {
                desc0 ->
                (currentMarker.tag as MutableMap<String, Any>)["description"] = desc0.toString()
            }
            tit.let {
                t->
                (currentMarker.tag as MutableMap<String, Any>)["title"] = t.toString()
            }
            db.updateRoute(idRoute, copyMarkerList){
                if (it){
                    Log.i("Maps", "Todo ok")
                }
                else{
                    (currentMarker.tag as MutableMap<String,Any>).putAll(tag)
                }
            }

        }

        // hacer para actualizar los datos de las rutas, necesitaré de los tres puntitos para desplegar el popup quizás
    }




    private lateinit var nMap: GoogleMap
    private lateinit var routeData : HashMap<String, Any>
    private lateinit var iconGenerator: IconGenerator

    private var newRoute: Boolean = false

    private var initialZoom: Float = 14f
    private lateinit var initialPosition: LatLng


    lateinit var routepopup: routesPopUp

    val REQUEST_CODE_MARKERMENU = 1
    val REQUEST_CODE_ROUTEMENU = 2


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
            idRoute = routeData["id"] as String
            Log.i("Maps", "Esta es mi ruta: $routeData")

        }
        else{
            initialPosition = HomeActivity.lastMapPosition
            initialZoom = HomeActivity.lastMapZoom
            idRoute = intent.getStringExtra("idRoute").toString()
        }

        editRouteBtnShowData.setOnClickListener {
            routepopup = routesPopUp(routeData["title"] as String, routeData["description"] as String,
                routeData["id"] as String, this, this.applicationContext, this.layoutInflater)

            routepopup.show(true)
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
            currentMarker = marker
            Log.i("Maps", "He entrado y el marcador es: $dataPointMenu.currentMarker")

            dataPointMenu.setInfo(title,description, idRoute,idMarker,this,this.applicationContext,this.layoutInflater)
            dataPointMenu.showMenu(true)

            true
        }

        nMap.setOnMapClickListener { latLng ->
            val marker = nMap.addMarker(MarkerOptions().position(latLng))
            marker?.let{
                val uidRandom = UUID.randomUUID().toString()
                marker.tag = mutableMapOf(
                    "id" to uidRandom,
                    "title" to "",
                    "description" to ""
                )
                marker.setIcon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon()))
                markerList.add(marker)

                db.updateRoute(idRoute, markerList){
                    if (!it){
                        Log.i("Maps", "Error en la subida")
                        marker.remove()
                        markerList.remove(marker)
                    }
                    else{
                        dataPointMenu.setInfo("","", idRoute,uidRandom,this,this.applicationContext,this.layoutInflater)
                        dataPointMenu.showMenu(true)
                    }
                }
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_MARKERMENU) {
            val ref: Uri = data?.data!!
            dataPointMenu.uploadImageShow(ref)
        } else if(resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_ROUTEMENU) {
            routepopup.uploadPhotoDBShow(data?.data!!)
        }
    }

    private fun showRoute(){
        markerList.clear()
        Log.i("Maps", routeData.toString())
        for((i,marker0) in (routeData["markers"] as List<*>).withIndex()){
            val latLng: LatLng = positionToLatLng(marker0 as Map<String, Any>)
            val marker = nMap.addMarker(MarkerOptions().position(latLng))

            if (i == 0) { iconGenerator.setStyle(IconGenerator.STYLE_PURPLE) }
            marker?.let {
                marker.tag = marker0["tag"]
                marker.setIcon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon()))
                markerList.add(marker)
            }
            iconGenerator.setStyle(IconGenerator.STYLE_BLUE)
        }
    }

    private fun positionToLatLng(position: Map<String, Any>) : LatLng {
        return LatLng(position["lat"] as Double, position["lng"]  as Double)
    }

}