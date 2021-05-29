package com.jeva.jeva.home.tabs

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
import com.jeva.jeva.database.Database
import com.jeva.jeva.GestionarPermisos
import com.jeva.jeva.ObtencionLocalizacion
import com.jeva.jeva.R
import com.jeva.jeva.home.HomeActivity
import com.jeva.jeva.home.ShowRouteActivity
import kotlinx.android.synthetic.main.fragment_explore.*
import java.io.Serializable


class ExploreFragment : Fragment(),OnMapReadyCallback {

    private lateinit var nMap : GoogleMap
    private val db = Database()
    private val obtencionLocalizacion = ObtencionLocalizacion()

    private var indexRoute: Int? = null
    private var idRoute: String? = null

    private var currentRoute = mutableListOf<Marker>()         // lista que almacena los marcadores de la ruta seleccionada, para así poder eliminarlos posteriormente
    private var routesFirstMarker = mutableListOf<Marker>()        // los marcadores iniciales de cada ruta, se usa para hacerlos invisibles

    private lateinit var routes: List<Map<String, Any>>         //es una lista que almacena lo que se devuelve de la BD
    private lateinit var iconGenerator: IconGenerator           //generador de iconos, se inicializa cuando se inicia el maps
    private var inRoute = false                                  //variable para ver si estamos dentro de una ruta en el mapa


    companion object {
        var mapView : SupportMapFragment?=null
    }



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_explore, container, false)
        if (savedInstanceState == null) {
            mapView = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
            mapView?.getMapAsync(this)
        }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapsBtnLocate.setOnClickListener {
            //db.newRoute(markerList){}//temporal, por eso no hay nada en el callback
            posicionarMapa()
        }
        //boton para ir al fragment visualizador de ruta.
        btnGoShowMap.setOnClickListener {
            indexRoute?.let {
                indexRoute0 ->
                Log.i("Maps", indexRoute.toString())
                val intent = Intent(context, ShowRouteActivity :: class.java).apply {
                    putExtra("routeData",  routes[indexRoute0] as Serializable)
                    putExtra("mapZoom", nMap.cameraPosition.zoom)
                }
                startActivity(intent)
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        nMap = googleMap
        nMap.moveCamera(CameraUpdateFactory.newLatLngZoom(HomeActivity.lastMapPosition, HomeActivity.lastMapZoom))
        iconGenerator = IconGenerator(activity)
        if(!inRoute) { showRoutes() }


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
                btnGoShowMap.visibility = View.VISIBLE //pongo visible el botón que me lleva al nuevo fragment
                inRoute = true
                idRoute = routes[marker.tag as Int]["id"] as String

                showRouteMarkers(marker.tag as Int) //siempre al final de cada método
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
                val position = m["position"] as Map<String, Any>?
                position?.let { position0 ->
                    val latLang =  positionToLatLng(position0)
                    val marker = nMap.addMarker(MarkerOptions().position(latLang))

                    marker?.let {
                        it.tag = i
                        it.setIcon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon()))
                        routesFirstMarker.add(it)
                    }
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
        Log.i("Maps", markers.toString())

        for ((i,marker0) in markers.withIndex()){
            val latLng: LatLng = positionToLatLng(marker0)
            val marker = nMap.addMarker(MarkerOptions().position(latLng))
            listaLatLng.add(latLng)

            if (i == 0) {
                iconGenerator.setStyle(STYLE_PURPLE)
                Log.i("Maps", "He entrado")
            }
            else { iconGenerator.setStyle(STYLE_BLUE) }
            marker?.let {
                marker.tag = marker0["tag"] as MutableMap<*, *>
                marker.setIcon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon()))
                currentRoute.add(marker)
            }

        }
        nMap.addPolyline(PolylineOptions().addAll(listaLatLng).visible(true))
    }

    private fun posicionarMapa() {
        Log.i("Pruebas", "HOLAAA?!!??!?!")
        GestionarPermisos.requestLocationPermissions(this.requireActivity())
        obtencionLocalizacion.localizacion(this.requireActivity())
            .addOnSuccessListener { location ->
                location?.let {
                    val zoom = 10F
                    nMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), zoom))
                }
                Log.i("Pruebas", location?.toString() + "No entendi")
            }
    }

    override fun onDestroyView() {
        HomeActivity.lastMapPosition = nMap.cameraPosition.target
        HomeActivity.lastMapZoom = nMap.cameraPosition.zoom
        super.onDestroyView()
    }

}