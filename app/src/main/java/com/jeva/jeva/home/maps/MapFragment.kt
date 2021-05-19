package com.jeva.jeva.home.maps

import android.content.Intent
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
import com.jeva.jeva.home.ShowRoute
import kotlinx.android.synthetic.main.fragment_maps.*
import java.io.Serializable
import java.util.*


class MapFragment : Fragment(),OnMapReadyCallback {

    private lateinit var nMap : GoogleMap
    private val db = Database()
    val obtencionLocalizacion = ObtencionLocalizacion()

    private var index = 0    //usar markerIndex                                    //index para llevar cuantos marcadores hemos marcado en el mapa
    private var markerList = mutableListOf<Marker>()            // esta var irá en otro fragment, pero es para almacenar los puntos seleccionados en el mapa
    private var indexRoute: Int? = null

    private var currentRoute = mutableListOf<Marker>()         // lista que almacena los marcadores de la ruta seleccionada, para así poder eliminarlos posteriormente
    private var routesFirstMarker = mutableListOf<Marker>()        // los marcadores iniciales de cada ruta, se usa para hacerlos invisibles

    private lateinit var routes: List<Map<String, Any>>         //es una lista que almacena lo que se devuelve de la BD
    private lateinit var iconGenerator: IconGenerator           //generador de iconos, se inicializa cuando se inicia el maps
    private var inRoute = false                                  //variable para ver si estamos dentro de una ruta en el mapa
    private lateinit  var polyline: Polyline

    //private var latlng0 = LatLng(0.0,0.0)        //almacenará la posición actual. Por defecto: (0.0,0.0)

    companion object {
        var mapView : SupportMapFragment?=null
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
            //db.newRoute(markerList){}//temporal, por eso no hay nada en el callback
            posicionarMapa()
        }
        //boton para ir al fragment visualizador de ruta.
        btnGoShowMap.setOnClickListener {
            indexRoute?.let {
                indexRoute0 ->
                Log.i("Maps", indexRoute.toString())
                val intent = Intent(context, ShowRoute :: class.java).apply {
                    putExtra("routeData",  routes[indexRoute0] as Serializable)
                    putExtra("mapZoom", nMap.cameraPosition.zoom)
                }
                startActivity(intent)
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        nMap = googleMap
        iconGenerator = IconGenerator(activity)
        if(!inRoute) showRoutes()

        nMap.setOnMapClickListener { latLng ->
            index += 1
            val marker = nMap.addMarker(MarkerOptions().position(latLng).title("Hola $index"))
            marker?.let{
                marker.tag = mutableMapOf(
                    "id" to UUID.randomUUID().toString(),
                    "index" to index,
                    "title" to "",
                    "description" to ""
                )
                marker.setIcon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon((marker.tag as MutableMap<String, Any>)["index"].toString())))
                markerList.add(marker)
            }
        }



        nMap.setOnMapLongClickListener {
            if(inRoute) {
                nMap.clear()
                currentRoute.clear()
                inRoute = false
                showRoutes()
                btnGoShowMap.visibility = View.INVISIBLE //lo volvemos a poner ne invisible
            }
        }

        nMap.setOnMarkerClickListener {
            marker ->
            if(!inRoute) {//variable global que nos dice si estamos viendo todas las rutas, o los puntos de una
                indexRoute = marker.tag as Int
                showRouteMarkers(marker.tag as Int)
                btnGoShowMap.visibility = View.VISIBLE //pongo visible el botón que me lleva al nuevo fragment
                inRoute = true
            }
            else{
                val tag = marker.tag as Map<*, *>
                val title: String = tag["title"] as String
                val description: String = tag["description"] as String
                //dataPointMenu.setInfo(title,description, arrayOf(),this)
                //dataPointMenu.showMenu()
            }
            true
        }

        nMap.setOnCameraIdleListener {

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
                routes = it
                //routesId = it.map { par -> par.first} as List<String>
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

    private fun posicionarMapa() {
        GestionarPermisos.requestLocationPermissions(this.requireActivity())
        obtencionLocalizacion.localizacion(this.requireActivity())
            .addOnSuccessListener { location ->
                location?.let {
                    Log.i("Maps", "Esta mi posicion: $it")
                    val zoom = 10F
                    nMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), zoom))
                }
            }
    }

}