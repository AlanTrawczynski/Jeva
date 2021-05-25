package com.jeva.jeva.home

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.ui.IconGenerator
import com.jeva.jeva.Database
import com.jeva.jeva.R
import com.jeva.jeva.images.dataPointMenu
import kotlinx.android.synthetic.main.activity_show_route.*

class ShowRoute : AppCompatActivity(), OnMapReadyCallback {

    private val db = Database()

    private lateinit var nMap: GoogleMap

    private lateinit var routeData : HashMap<String, Any>
    private lateinit var iconGenerator: IconGenerator
    private var initialZoom: Float = 14f

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
            btnEditMode.visibility = View.VISIBLE
        }
    }

    private fun positionToLatLng(position: Map<String, Any>) : LatLng {
        return LatLng(position["lat"] as Double, position["lng"]  as Double)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        nMap = googleMap
        iconGenerator = IconGenerator(this)
        showRoute()
        nMap.apply {
            moveCamera(CameraUpdateFactory.newLatLngZoom(positionToLatLng(routeData["position"] as Map<String, Any>), initialZoom))
        }

        nMap.setOnMarkerClickListener {
            marker ->
            val tag = marker.tag as Map<*, *>
            val title: String = tag["title"] as String
            val description: String = tag["description"] as String
            val idMarker = tag["id"] as String

            dataPointMenu.setInfo(title,description, arrayOf(),routeData["id"] as String,idMarker,this,this.applicationContext)
            dataPointMenu.showMenu(this.layoutInflater,null,false)
            true
        }
    }

    private fun showRoute(){
        Log.i("Maps", routeData.toString())
        for((i,marker0) in (routeData["markers"] as List<*>).withIndex()){
            val latLng: LatLng = positionToLatLng(marker0 as Map<String, Any>)
            val marker = nMap.addMarker(MarkerOptions().position(latLng))

            if (i == 0) { iconGenerator.setStyle(IconGenerator.STYLE_PURPLE)
                //iconGenerator.setBackground(resources.getDrawable(R.drawable.ey))
            }
            else { iconGenerator.setStyle(IconGenerator.STYLE_BLUE) }
            marker?.let {
                marker.tag = marker0["tag"] as MutableMap<*, *>
                marker.setIcon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon("Ha funcionado")))
                //currentRoute.add(marker)
                //listaLatLng.add(latLng)
            }
        }
    }
}