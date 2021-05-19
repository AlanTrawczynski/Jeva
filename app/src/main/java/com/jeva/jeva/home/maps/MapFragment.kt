package com.jeva.jeva.home.maps

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.ui.IconGenerator
import com.google.maps.android.ui.IconGenerator.*
import com.jeva.jeva.Database
import com.jeva.jeva.GestionarPermisos
import com.jeva.jeva.ObtencionLocalizacion
import com.jeva.jeva.R
import com.jeva.jeva.images.dataPointMenu
import kotlinx.android.synthetic.main.fragment_maps.*


class MapFragment : Fragment(),OnMapReadyCallback {

    private lateinit var nMap : GoogleMap
    private val db = Database()

    private var index = 0    //usar markerIndex                                    //index para llevar cuantos marcadores hemos marcado en el mapa
    private var markerList = mutableListOf<Marker>()            // esta var irá en otro fragment, pero es para almacenar los puntos seleccionados en el mapa

    private var currentRoute = mutableListOf<Marker>()         // lista que almacena los marcadores de la ruta seleccionada, para así poder eliminarlos posteriormente
    private var routesFirstMarker = mutableListOf<Marker>()        // los marcadores iniciales de cada ruta, se usa para hacerlos invisibles

    private lateinit var routes: List<Map<String, Any>>         //es una lista que almacena lo que se devuelve de la BD
    private lateinit var routesId: List<String>
    private lateinit var iconGenerator: IconGenerator           //generador de iconos, se inicializa cuando se inicia el maps
    private var inRoute = false                                  //variable para ver si estamos dentro de una ruta en el mapa
    private lateinit var inRouteId: String
    private lateinit  var polyline: Polyline

    private var latlng = LatLng(0.0,0.0)        //almacenará la posición actual. Por defecto: (0.0,0.0)

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

            /* DESCOMENTAR SI SE QUIERE QUE SE ABRA EL MAPA EN TU POS
            GestionarPermisos.requestLocationPermissions(this.requireActivity())
            ObtencionLocalizacion.localizacion(this.requireActivity())
                .addOnSuccessListener {
                    latlng = LatLng(it.latitude,it.longitude)
                }
                .addOnCompleteListener {
                    //cuando se obtenga la localización se representa el mapa. NO antes.
                    mapView?.getMapAsync(this)
                } */
            mapView?.getMapAsync(this)
        }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        buttonNewRoute.setOnClickListener {
            db.newRoute(markerList){}//temporal, por eso no hay nada en el callback
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        nMap = googleMap
        iconGenerator = IconGenerator(activity)
        if(!inRoute) showRoutes()

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

        /* DESCOMENTAR SI SE QUIERE QUE SE ABRA EL MAPA EN TU POS
        nMap.apply {
            moveCamera(CameraUpdateFactory.newLatLngZoom(latlng,10f))
        }*/

        nMap.setOnMapLongClickListener {
            if(inRoute) {
                nMap.clear()
                currentRoute.clear()
                inRoute = false
                showRoutes()
            }
        }

        nMap.setOnMarkerClickListener {
            marker ->
            if(!inRoute) {//variable global que nos dice si estamos viendo todas las rutas, o los puntos de una ruta
                inRouteId = routesId[marker.tag as Int]
                showRouteMarkers(marker.tag as Int)
                inRoute = true
            }
            else {
                //Aquí irá que al seleccionar un punto, muestre la información.
                var tag = marker.tag as Map<String, Any>
                var title: String = tag["title"] as String
                var description: String = tag["description"] as String
                dataPointMenu.setInfo(title,description, arrayOf(),this)
                dataPointMenu.showMenu(true)
            }
            true
        }

        nMap.setOnCameraIdleListener {
            Log.i("Maps", "Se ha candelado el movimiento 2: " + nMap.projection.visibleRegion.latLngBounds.center)
            Log.i("Maps", "Se ha candelado el movimiento 2: " + nMap.projection.visibleRegion.latLngBounds)

            if (!inRoute){
                for (m in routesFirstMarker){
                    m.remove()
                }
                routesFirstMarker.clear()
                showRoutes()
            }
        }
    }

    private fun positionToLatLng(position: Map<String, Any>) : LatLng {
        return LatLng(position["lat"] as Double, position["lng"]  as Double)
    }

    //función para conseguir de la bd todas las rutas de la BD (se transformará a solo los vecinos
    private fun showRoutes(){

        fun show(){
            iconGenerator.setStyle(STYLE_RED)
            for ((i,m) in routes.withIndex()){
                Log.i("Maps", m["position"].toString())
                val position = m["position"] as Map<String, Any>
                val latLang =  positionToLatLng(position)
                val marker = nMap.addMarker(MarkerOptions().position(latLang))

                marker?.let {
                    it.tag = i
                    it.setIcon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon(i.toString())))
                    routesFirstMarker.add(it)
                }
            }
        }

        db.getNearbyRoutes(nMap.projection.visibleRegion.latLngBounds){
            if (it != null) {
                routes = it.map { par -> par.second }
                routesId = it.map { par -> par.first} as List<String>
                show()
            } else {
                Log.e("Maps", "No se ha encontrado ruta")
            }
        }
    }

    //Muestra los puntos de la ruta seleccionada en el mapa
    private fun showRouteMarkers(ind : Int) {
        nMap.clear()
        val listaLatLng = mutableListOf<LatLng>()
        val markers = (routes[ind]["markers"] as List<Map<String, Any>>)

        for ((i,marker0) in markers.withIndex()){
            val latLng: LatLng = positionToLatLng(marker0)
            val marker = nMap.addMarker(MarkerOptions().position(latLng))

            if (i == 0) { iconGenerator.setStyle(STYLE_PURPLE) }
            else { iconGenerator.setStyle(STYLE_BLUE) }
            marker?.let {
                marker.tag = marker0["tag"] as MutableMap<*, *>
                marker.setIcon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon("Ha funcionado")))
                currentRoute.add(marker)
                listaLatLng.add(latLng)
            }

        }
        polyline = nMap.addPolyline(PolylineOptions().addAll(listaLatLng).visible(true))
    }
}