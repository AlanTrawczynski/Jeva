package com.jeva.jeva.home

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.ui.IconGenerator
import com.jeva.jeva.database.Database
import com.jeva.jeva.R
import com.jeva.jeva.images.dataPointMenu
import com.jeva.jeva.images.routesPopUp
import kotlinx.android.synthetic.main.activity_show_route.*
import java.io.Serializable

class ShowRouteActivity : AppCompatActivity(), OnMapReadyCallback {

    private val db = Database()

    private lateinit var nMap: GoogleMap

    private lateinit var routeData : HashMap<String, Any>
    private lateinit var iconGenerator: IconGenerator
    private var initialZoom: Float = 14f

    private var ready = false

    private val REQUEST_CODE: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_show_route)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.showMap) as SupportMapFragment
        mapFragment.getMapAsync(this)

        initialZoom = intent.getFloatExtra("mapZoom", 14f)
        routeData = intent.getSerializableExtra("routeData") as HashMap<String, Any>

        val idUser:String = db.getCurrentUserUid()
        val ownerId= routeData["owner"] as String

        if (idUser == ownerId){
            showRouteBtnGoEdit.visibility = View.VISIBLE
        }
        showRouteBtnGoEdit.setOnClickListener {
            val intent = Intent(this, EditRouteActivity :: class.java).apply {
                putExtra("routeData",  routeData as Serializable)
                putExtra("mapZoom", nMap.cameraPosition.zoom)
                putExtra("newRoute", false)
            }
            startActivity(intent)
        }
        showRouteBtnShowData.setOnClickListener {
            var popup: routesPopUp = routesPopUp(routeData["title"] as String, routeData["description"] as String,
                    routeData["id"] as String, this, this.applicationContext, this.layoutInflater)
            popup.show(false)
        }
        this.title = "Ruta"
    }

    private fun positionToLatLng(position: Map<String, Any>) : LatLng {
        return LatLng(position["lat"] as Double, position["lng"]  as Double)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        ready = true
        nMap = googleMap
        iconGenerator = IconGenerator(this)
        showRoute()
        nMap.moveCamera(CameraUpdateFactory.newLatLngZoom(positionToLatLng(routeData["position"] as Map<String, Any>), initialZoom))

        nMap.setOnMarkerClickListener {
            marker ->
            val tag = marker.tag as Map<*, *>
            val title: String = tag["title"] as String
            val description: String = tag["description"] as String
            val idMarker = tag["id"] as String
            val idRoute = routeData["id"] as String

            dataPointMenu.setInfo(title,description, idRoute,idMarker,this,this.applicationContext,this.layoutInflater)
            dataPointMenu.showMenu(false)
            true
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE) {
            val ref: Uri = data?.data!!
            dataPointMenu.uploadImageShow(ref)
        }
    }

    private fun showRoute(){
        Log.i("Maps", routeData.toString())
        val listLatLng = mutableListOf<LatLng>()
        for((i,marker0) in (routeData["markers"] as List<*>).withIndex()){
            val latLng: LatLng = positionToLatLng(marker0 as Map<String, Any>)
            val marker = nMap.addMarker(MarkerOptions().position(latLng))

            listLatLng.add(latLng)

            if (i == 0) { iconGenerator.setStyle(IconGenerator.STYLE_PURPLE)
                //iconGenerator.setBackground(resources.getDrawable(R.drawable.ey))
            }
            else { iconGenerator.setStyle(IconGenerator.STYLE_BLUE) }
            marker?.let {
                marker.tag = marker0["tag"] as MutableMap<*, *>
                marker.setIcon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon()))
            }
        }
        val polyline = nMap.addPolyline(PolylineOptions().addAll(listLatLng).visible(true))

    }


}