package com.jeva.jeva.home

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.ui.IconGenerator
import com.jeva.jeva.R
import com.jeva.jeva.database.Database
import com.jeva.jeva.images.dataPointMenu
import com.jeva.jeva.images.routesPopUp
import kotlinx.android.synthetic.main.activity_edit_route.*
import java.util.*
import kotlin.collections.HashMap

class EditRouteActivity : AppCompatActivity(), OnMapReadyCallback {

    companion object {
        private val db = Database()
        lateinit var currentMarker: Marker
        private var markers = mutableListOf<Marker>()
        private lateinit var idRoute: String
        private lateinit var routeData : HashMap<String, Any>
        lateinit var polyline : Polyline

        //private var markerIndex = 0

        fun deleteMarker(){
            val copyMarkerList = mutableListOf<Marker>()
            copyMarkerList.addAll(markers)
            Log.i("Maps", "todo ok y el marcador es: $currentMarker")
            Log.i("Maps", "La lista es: $markers")
            copyMarkerList.remove(currentMarker)
            db.updateRoute(idRoute, copyMarkerList){
                if(!it){
                    Log.i("Maps", "ERROR")
                }
                else{
                    markers.remove(currentMarker)
                    currentMarker.remove()
                    refreshPolyline()
                    Log.i("Maps", "Todo ok")
                }
            }
        }

        // subir nuevos puntos a la bd hecho

        // actualizar los datos de los puntos

        fun updateMarkersRoute(desc: String? = null, tit: String? = null){
            Log.i("Pruebas", "He entrado en el updateMarkersRoute")
            val copyMarkerList = mutableListOf<Marker>()
            copyMarkerList.addAll(markers)
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
                Log.i("Pruebas", "He entrado profundo en updateMarkersRoute $it")
                if (it){
                    Log.i("Maps", "Todo ok")
                }
                else{
                    (currentMarker.tag as MutableMap<String,Any>).putAll(tag)
                }
            }

        }

        fun updateRoute(desc: String? = null, tit: String? = null){
            Log.i("Pruebas", "He entrado en el updateRoute0")
            db.updateRoute(idRoute, title = tit, description = desc){
                result ->
                Log.i("Pruebas", "He entrado aún más dentro $result")
                if (result){
                    Log.i("Maps", "Todo ok")
                    tit?.let{ tit0->
                        routeData["title"] = tit0
                    }
                    desc?.let { desc0 ->
                        routeData["description"] = desc0
                    }

                }
                else{
                    Log.i("Maps", "Algo ha fallado, quizás lanzar fallo")
                }
            }

        }

        fun deleteRoute(activity: Activity){
            db.deleteRoute(routeId = idRoute){
                if(it){
                    Log.i("Pruebas", "Muerete actividad")
                    activity.finish()
                }
            }
        }

        private fun refreshPolyline() {
            polyline.points = markers.map { it.position }
        }
        // hacer para actualizar los datos de las rutas, necesitaré de los tres puntitos para desplegar el popup quizás
    }




    private lateinit var nMap: GoogleMap
    private lateinit var iconGenerator: IconGenerator

    private var newRoute: Boolean = false

    private var initialZoom: Float = 10f
    private lateinit var initialPosition: LatLng


    private lateinit var routepopup: routesPopUp

    private val REQUEST_CODE_MARKERMENU = 1
    private val REQUEST_CODE_ROUTEMENU = 2


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

            routeData = intent.getSerializableExtra("routeData") as HashMap<String, Any>
            idRoute = routeData["id"] as String

            Log.i("Maps", "Esta es mi ruta: $routeData")
            if ((routeData["markers"] as List<*>).isEmpty()){
                initialPosition = HomeActivity.lastMapPosition
                initialZoom = HomeActivity.lastMapZoom
            }
            else{
                initialZoom = intent.getFloatExtra("mapZoom", 10f)
                initialPosition = mapToLatLng(routeData["position"] as HashMap<String,Any>)
            }

        }
        else{
            initialPosition = HomeActivity.lastMapPosition
            initialZoom = HomeActivity.lastMapZoom
            idRoute = intent.getStringExtra("idRoute").toString()
            routeData = mutableMapOf<String, Any>(
                "id" to idRoute,
                "title" to "",
                "description" to "",
                "owner" to db.getCurrentUserUid()
            ) as HashMap<String, Any>
        }

        editRouteBtnShowData.setOnClickListener {
            routepopup = routesPopUp(routeData["title"] as String, routeData["description"] as String,
                routeData["id"] as String, this, this.applicationContext, this.layoutInflater)

            routepopup.show(true, this)
        }

        this.title = "Editar"

    }

    override fun onMapReady(googleMap: GoogleMap) {
        nMap = googleMap
        iconGenerator = IconGenerator(this)
        iconGenerator.setStyle(IconGenerator.STYLE_BLUE)

        if(newRoute){
            polyline = nMap.addPolyline(PolylineOptions().color(Color.parseColor("#AAc2c3c9")).visible(true))

        }
        else{
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

            if(markers.isNotEmpty()) {
                setMarkerColorDefault()
                markers.last().setIcon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon("${markers.size}")))
            }

            marker?.let{
                val markerId = UUID.randomUUID().toString()
                val markerNumber = markers.size + 1

                setMarkerColorHighlight()
                marker.setIcon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon(markerNumber.toString())))
                marker.tag = mutableMapOf(
                    "id" to markerId,
                    "title" to "",
                    "description" to ""
                )
                markers.add(marker)
                refreshPolyline()

                db.updateRoute(idRoute, markers){
                    if (!it){
                        Log.i("Maps", "Error en la subida")
                        marker.remove()
                        markers.remove(marker)
                    }
                    else{
                        currentMarker = marker
                        dataPointMenu.setInfo("","", idRoute, markerId,this,this.applicationContext,this.layoutInflater)
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

    private fun showRoute() {
        val routeMarkers = routeData["markers"] as List<Map<String, Any>>
        setMarkerColorDefault()
        for((i, routeMarker) in routeMarkers.withIndex()) {
            val latLng: LatLng = mapToLatLng(routeMarker)
            val mapMarker = nMap.addMarker(MarkerOptions().position(latLng))

            mapMarker?.let {
                mapMarker.tag = routeMarker["tag"]
                if (i == routeMarkers.size-1) {
                    setMarkerColorHighlight()
                }
                mapMarker.setIcon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon("${i+1}")))
                markers.add(mapMarker)
            }
        }
        polyline = nMap.addPolyline(
            PolylineOptions()
            .addAll(markers.map { it.position })
            .color(Color.parseColor("#AAc2c3c9"))
            .visible(true))
    }


    private fun mapToLatLng(position: Map<String, Any>) : LatLng {
        return LatLng(position["lat"] as Double, position["lng"] as Double)
    }

    private fun setMarkerColorHighlight() {
        iconGenerator.setColor(Color.parseColor("#FF03A9F5"))
    }

    private fun setMarkerColorDefault() {
        iconGenerator.setColor(Color.parseColor("#FFc2c3c9"))
    }

    override fun onDestroy() {
        super.onDestroy()

        markers.clear()
        Log.i("Pruebas", "Me he destruido y he limpiado la lista")
    }

}