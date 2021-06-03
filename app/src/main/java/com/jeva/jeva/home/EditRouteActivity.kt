package com.jeva.jeva.home

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
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
import com.jeva.jeva.images.dataPointMenu.Companion.context
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
        private lateinit var iconGenerator: IconGenerator

        fun deleteMarker(){
            val copyMarkerList = mutableListOf<Marker>()
            copyMarkerList.addAll(markers)
            copyMarkerList.remove(currentMarker)
            db.updateRoute(idRoute, copyMarkerList){
                if(!it){
                    Toast.makeText(context, context.getString(R.string.deleteMarkerError), Toast.LENGTH_SHORT).show()
                }
                else{
                    markers.remove(currentMarker)
                    currentMarker.remove()
                    reloadMarkerStyle()
                    refreshPolyline()
                }
            }
        }

        fun updateMarkersRoute(desc: String? = null, tit: String? = null){
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
                if (!it){
                    (currentMarker.tag as MutableMap<String,Any>).putAll(tag)
                    Toast.makeText(context, context.getString(R.string.updateMarkerError), Toast.LENGTH_SHORT).show()
                }
            }

        }

        fun updateRoute(desc: String? = null, tit: String? = null){
            db.updateRoute(idRoute, title = tit, description = desc){
                result ->
                if (result){
                    tit?.let{ tit0->
                        routeData["title"] = tit0
                    }
                    desc?.let { desc0 ->
                        routeData["description"] = desc0
                    }
                }
                else {
                    Toast.makeText(context, context.getString(R.string.udateRouteError), Toast.LENGTH_SHORT).show()
                }
            }

        }

        private fun refreshPolyline() {
            polyline.points = markers.map { it.position }
        }

        private fun reloadMarkerStyle(){
            setMarkerColorDefault()
            for ((index,m) in markers.withIndex()){
                m.setIcon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon("${index+1}")))
            }
            if(markers.isNotEmpty()) {
                setMarkerColorHighlight()
                markers.last().setIcon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon()))
            }
        }

        private fun setMarkerColorHighlight() {
            iconGenerator.setColor(Color.parseColor("#FF03A9F5"))
        }

        private fun setMarkerColorDefault() {
            iconGenerator.setColor(Color.parseColor("#FFc2c3c9"))
        }
    }




    private lateinit var nMap: GoogleMap

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
        if(!newRoute){

            routeData = intent.getSerializableExtra("routeData") as HashMap<String, Any>
            idRoute = routeData["id"] as String

            if ((routeData["markers"] as List<*>).isEmpty()){
                initialPosition = HomeActivity.lastMapPosition
                initialZoom = HomeActivity.lastMapZoom
            }
            else{
                initialZoom = intent.getFloatExtra("mapZoom", 5f)
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

        editRouteBtnBack.setOnClickListener { finish() }

        editRouteBtnShowData.setOnClickListener {
            routepopup = routesPopUp(routeData["title"] as String, routeData["description"] as String,
                routeData["id"] as String, this, this.applicationContext, this.layoutInflater)

            routepopup.show(true)
        }

    }

    override fun onMapReady(googleMap: GoogleMap) {
        nMap = googleMap
        nMap.uiSettings.isRotateGesturesEnabled = false
        iconGenerator = IconGenerator(this)
        iconGenerator.setStyle(IconGenerator.STYLE_BLUE)

        if(newRoute){
            polyline = nMap.addPolyline(PolylineOptions().color(Color.parseColor("#AAc2c3c9")).visible(true))
            routepopup = routesPopUp(routeData["title"] as String, routeData["description"] as String,
                routeData["id"] as String, this, this.applicationContext, this.layoutInflater)

            routepopup.show(true)

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
                marker.isDraggable = true
                refreshPolyline()

                db.updateRoute(idRoute, markers){
                    if (!it){
                        marker.remove()
                        markers.remove(marker)

                        Toast.makeText(context, getString(R.string.createMarkerError), Toast.LENGTH_SHORT).show()
                    }
                    else{
                        currentMarker = marker
                        dataPointMenu.setInfo("","", idRoute, markerId,this,this.applicationContext,this.layoutInflater)
                        dataPointMenu.showMenu(true)
                    }
                }
            }
        }

        nMap.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener{
            lateinit var initialMarkerPosition: LatLng
            override fun onMarkerDragStart(p0: Marker) {
                refreshPolyline()
                initialMarkerPosition = p0.position
            }

            override fun onMarkerDrag(p0: Marker) {
                refreshPolyline()
            }

            override fun onMarkerDragEnd(p0: Marker) {
                db.updateRoute(idRoute, markers = markers){ it0 ->
                    if (it0){
                        refreshPolyline()
                    }
                    else{
                        p0.position = initialMarkerPosition
                        Toast.makeText(context, getString(R.string.dragMarkerError), Toast.LENGTH_SHORT).show()
                    }

                }
            }

        })

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
                mapMarker.isDraggable = true
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


    override fun onDestroy() {
        super.onDestroy()
        markers.clear()
    }

}