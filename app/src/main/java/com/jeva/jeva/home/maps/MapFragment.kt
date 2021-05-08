package com.jeva.jeva.home.maps

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.jeva.jeva.R
import com.google.android.gms.maps.*
import com.google.android.gms.maps.OnMapReadyCallback


class MapFragment : Fragment(),OnMapReadyCallback {

    private lateinit var nMap : GoogleMap

    companion object {
        var mapView : SupportMapFragment?=null
        val TAG: String = MapFragment::class.java.simpleName
        fun newInstance() = MapFragment()
    }



    override fun onSaveInstanceState(outState: Bundle) {
        outState.putFloat("zoom",nMap.cameraPosition.zoom)
        outState.putDouble("lat",nMap.cameraPosition.target.latitude)
        outState.putDouble("lon",nMap.cameraPosition.target.longitude)
        outState.putBundle("mapView", mapView?.arguments)
        super.onSaveInstanceState(outState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if(savedInstanceState != null){
            /*val coord = LatLng(savedInstanceState.getDouble("lat"),savedInstanceState.getDouble("lon"))
            val zoom: Float = savedInstanceState.getFloat("zoom")
            nMap.animateCamera(CameraUpdateFactory.newLatLngZoom(coord,zoom))*/
            mapView?.onCreate(savedInstanceState.getBundle("mapView"))
        }
        super.onCreate(savedInstanceState)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_maps, container, false)
        if (savedInstanceState == null) {
            mapView = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
            mapView?.getMapAsync(this)
        }
        return root
    }

    override fun onMapReady(googleMap: GoogleMap) {
        nMap = googleMap
    }

}