package com.jeva.jeva.home.maps

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.jeva.jeva.R
import com.google.android.gms.maps.*
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import com.google.firebase.firestore.GeoPoint
import com.google.maps.android.ui.IconGenerator
import com.google.maps.android.ui.IconGenerator.*
import com.jeva.jeva.Database
import kotlinx.android.synthetic.main.fragment_maps.*


class MapFragment : Fragment(),OnMapReadyCallback {

    private lateinit var nMap : GoogleMap
    private val db = Database()
    private var index = 0 //index para llevar cuantos marcadores hemos marcado en el mapa
    private var markerList = mutableListOf<Marker>() // esta var irá en otro fragmen, pero es para almacenar los puntos seleccionados en el mapa
    private var markerRout = mutableListOf<Marker>() // lista que almacena los marcadores de la ruta seleccionada, para así poder eliminarlos posteriormente
    private var markerListShow = mutableListOf<Marker>() // los marcadores iniciales de cada ruta, se usa para hacerlos invisibles
    private lateinit var listRoutes: List<Map<String, Any>> //es una lista que almacena lo que se devuelve de la BD
    private lateinit var iconGenerator: IconGenerator //generador de iconos, se inicializa cuando se inicia el maps
    private var inRoute = true //variable para ver si estamos dentro de una ruta en el mapa
    private lateinit  var polyline: Polyline

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        buttonNewRoute.setOnClickListener {
            db.newRoute(markerList){}
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        nMap = googleMap
        iconGenerator = IconGenerator(activity)
        routesInMap()

        /*nMap.setOnMapClickListener { latLng ->
            index += 1
            val marker = nMap.addMarker(MarkerOptions().position(latLng).title("Hola $index"))
            marker.tag = mutableMapOf(
                "index" to index,
                "title" to "",
                "description" to "",
                "photo" to mutableListOf<String>()
            )
            marker.setIcon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon((marker.tag as MutableMap<String, Any>)["index"].toString())))
            markerList.add(marker)
        }*/

        nMap.setOnMapLongClickListener {
            for(m in markerRout){
                m.remove()
            }
            polyline.remove()
            markerRout.clear()
            visibility(true)
            inRoute = true
        }

        nMap.setOnMarkerClickListener {
            marker ->
            if(inRoute) {//variable global que nos dice si estamos viendo todas las rutas, o los puntos de una ruta
                visibility(false)
                showMarkersRoutes(marker.tag as Int)
                inRoute = false
            }
            true
        }

        nMap.setOnCameraIdleListener {
            Log.i("Maps", "Se ha candelado el movimiento 2: " + nMap.projection.visibleRegion.latLngBounds.center)
            Log.i("Maps", "Se ha candelado el movimiento 2: " + nMap.projection.visibleRegion.latLngBounds)
        }
    }



    //función para conseguir de la bd todas las rutas de la BD (se transformará a solo los vecinos
    private fun routesInMap(){
        db.getAllRoutes { listRoutes0 ->
            if (listRoutes0 != null) {
                listRoutes = listRoutes0
                showRoutesInMap(listRoutes0)
            } else {
                Log.e("Maps", "No se ha encontrado ruta")
            }
        }
    }

    //Muestra los puntos iniciales de cada ruta conseguida de la BD
    private fun showRoutesInMap(lm: List<Map<String, Any>>){

        for ((i,m) in lm.withIndex()){
            Log.i("Maps", m["position"].toString())
            val position = m["position"] as Map<*, *>
            val lat = position["lat"] as Double
            val lng = position["lng"] as Double
            val marker = nMap.addMarker(MarkerOptions().position(LatLng(lat, lng)))
            marker.tag = i
            iconGenerator.setStyle(STYLE_RED)
            marker.setIcon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon(i.toString())))
            markerListShow.add(marker)
        }
    }

    //Muestra los puntos de la ruta seleccionada en el mapa
    private fun showMarkersRoutes(i : Int) {
        var listaLatLng = mutableListOf<LatLng>()
        val markers : List<Map<String, Any>> = (listRoutes[i]["markers"]) as List<Map<String, Any>>
        for ((i,marker0) in markers.withIndex()){
            val lat = marker0["lat"] as Double
            val lng = marker0["lng"] as Double
            val marker = nMap.addMarker(MarkerOptions().position(LatLng(lat, lng)))
            if (i == 0)  iconGenerator.setStyle(STYLE_PURPLE) else iconGenerator.setStyle(STYLE_BLUE)
            marker.tag = marker0["tag"] as MutableMap<String, Any>
            marker.setIcon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon("Ha funcionado")))
            markerRout.add(marker)
            listaLatLng.add(LatLng(lat, lng))
        }
        polyline = nMap.addPolyline(PolylineOptions().addAll(listaLatLng).visible(true))
    }

    //función que visibiliza o invisibiliza los marcadores del inicio de las rutas
    private fun visibility(t: Boolean){
        for (m in markerListShow) {
            m.isVisible = t
        }
    }
}