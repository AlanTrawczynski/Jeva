package com.jeva.jeva.home.tabs

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.ui.IconGenerator
import com.google.maps.android.ui.IconGenerator.STYLE_BLUE
import com.google.maps.android.ui.IconGenerator.STYLE_RED
import com.jeva.jeva.GestionarPermisos
import com.jeva.jeva.ObtencionLocalizacion
import com.jeva.jeva.R
import com.jeva.jeva.database.Database
import com.jeva.jeva.home.HomeActivity
import com.jeva.jeva.home.ShowRouteActivity
import kotlinx.android.synthetic.main.fragment_explore.*
import java.io.Serializable


class ExploreFragment : Fragment(), OnMapReadyCallback {

    private lateinit var nMap : GoogleMap
    private val db = Database()
    private val obtencionLocalizacion = ObtencionLocalizacion()

    private var indexRoute: Int? = null
    private var idRoute: String? = null

    private var currentRoute = mutableListOf<Marker>()         // lista que almacena los marcadores de la ruta seleccionada, para así poder eliminarlos posteriormente
    private var routesFirstMarker = mutableListOf<Marker>()    // los marcadores iniciales de cada ruta, se usa para hacerlos invisibles

    private lateinit var routes: List<Map<String, Any>>         //es una lista que almacena lo que se devuelve de la BD
    private lateinit var iconGenerator: IconGenerator           //generador de iconos, se inicializa cuando se inicia el maps
    private var inRoute = false //variable para ver si estamos dentro de una ruta en el mapa
    private var firstExecution = true


    companion object {
        var mapView : SupportMapFragment?=null
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (inRoute) {
                        exitCurrentRoute()
                    }   else {
                        activity?.moveTaskToBack(true)
                    }
                }
            }
        activity?.onBackPressedDispatcher?.addCallback(this, callback)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_explore, container, false)
        if (savedInstanceState == null) {
            mapView = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
            mapView?.getMapAsync(this)
        }
        setHasOptionsMenu(true)
        return root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapsBtnLocate.setOnClickListener {
            posicionarMapa()
        }

        mapsBtnBack.setOnClickListener {
            exitCurrentRoute()
        }

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
        firstExecution = false
        nMap.moveCamera(CameraUpdateFactory.newLatLngZoom(HomeActivity.lastMapPosition, HomeActivity.lastMapZoom))
        iconGenerator = IconGenerator(activity)
        iconGenerator.setStyle(STYLE_BLUE)
        if(!inRoute) { showRoutes() }

        nMap.setOnMarkerClickListener {
            marker ->
            if(!inRoute) {
                indexRoute = marker.tag as Int
                val route = routes[indexRoute!!]
                idRoute = route["id"] as String
                inRoute = true

                showRouteMarkers(indexRoute!!)
                btnGoShowMap.visibility = View.VISIBLE
                mapsBtnBack.visibility = View.VISIBLE

                val routePosition = route["position"] as Map<String, Double>
                routePosition["lat"]?.let { lat ->
                    routePosition["lng"]?.let { lng ->
                        nMap.animateCamera(CameraUpdateFactory.newLatLng(LatLng(lat, lng)))
                    }
                }
            }
            true
        }

        nMap.setOnCameraIdleListener {
            if (!inRoute){
                routesFirstMarker.forEach { it.remove() }
                routesFirstMarker.clear()
                showRoutes()
            }
        }
    }


    private fun showRoutes() {
        fun getMarkerIcon(color: String): BitmapDescriptor {
            val hsv = FloatArray(3)
            Color.colorToHSV(Color.parseColor(color), hsv)
            return BitmapDescriptorFactory.defaultMarker(hsv[0])
        }

        fun show() {
            iconGenerator.setStyle(STYLE_RED)
            for ((i,m) in routes.withIndex()){
                val position = m["position"] as Map<String, Any>?

                position?.let { pos ->
                    val latLang =  mapToLatLng(pos)
                    val marker = nMap.addMarker(MarkerOptions()
                        .position(latLang)
                        .icon(getMarkerIcon(resources.getString(R.color.jeva_blue))))

                    marker?.let {
                        it.tag = i
                        routesFirstMarker.add(it)
                    }
                }
            }
        }

        db.getNearbyRoutes(nMap.projection.visibleRegion.latLngBounds) {
            if (it != null) {
                routes = it
                show()
            } else {
                Log.e("Maps", "No se ha encontrado ruta")
            }
        }
    }


    //Muestra los puntos de la ruta seleccionada en el mapa
    private fun showRouteMarkers(i : Int) {
        val listaLatLng = mutableListOf<LatLng>()
        val routeMarkers = (routes[i]["markers"] as List<Map<String, Any>>)

        nMap.clear()

        for ((j, routeMarker) in routeMarkers.withIndex()) {
            val latLng: LatLng = mapToLatLng(routeMarker)
            val mapMarker = nMap.addMarker(MarkerOptions().position(latLng))

            listaLatLng.add(latLng)
            mapMarker?.let {
                it.tag = routeMarker["tag"] as MutableMap<*, *>
                iconGenerator.setColor(if (j == 0) Color.parseColor("#FF03A9F5") else Color.parseColor("#FFc2c3c9"))
                it.setIcon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon((j+1).toString())))
                currentRoute.add(it)
            }
        }
    }


    private fun mapToLatLng(position: Map<String, Any>) : LatLng {
        return LatLng(position["lat"] as Double, position["lng"] as Double)
    }


    private fun posicionarMapa() {
        GestionarPermisos.requestLocationPermissions(this.requireActivity())
        obtencionLocalizacion.localizacion(this.requireActivity())
            .addOnSuccessListener { location ->
                location?.let {
                    val zoom = 10F
                    nMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), zoom))
                }
            }
    }


    private fun exitCurrentRoute() {
        nMap.clear()
        currentRoute.clear()
        inRoute = false

        showRoutes()
        btnGoShowMap.visibility = View.INVISIBLE
        mapsBtnBack.visibility = View.INVISIBLE
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        SettingsMenu.onCreateOptionsMenu(menu, inflater)
        super.onCreateOptionsMenu(menu, inflater)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        view?.let { v ->
            activity?.let { a ->
                context?.let { c ->
                    SettingsMenu.onOptionsItemSelected(item, v, a, c)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onDestroyView() {
        HomeActivity.lastMapPosition = nMap.cameraPosition.target
        HomeActivity.lastMapZoom = nMap.cameraPosition.zoom
        super.onDestroyView()
    }


    override fun onStart() {
        super.onStart()
        if (!firstExecution){
            exitCurrentRoute()
        }
    }

}