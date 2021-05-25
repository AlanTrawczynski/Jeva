package com.jeva.jeva.home.myroutes

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.model.LatLng
import com.jeva.jeva.*
import com.jeva.jeva.home.EditRoute
import com.jeva.jeva.home.HomeActivity
import com.jeva.jeva.home.ShowRoute
import java.io.Serializable

class MyRoutesFragment : Fragment(),Serializable {

    private val db : Database = Database()
    private val obtencionLocalizacion = ObtencionLocalizacion()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        val root : View = inflater.inflate(R.layout.fragment_my_routes, container, false)
        val buttonContainer = root.findViewById(R.id.myRoutesButtonContainer) as LinearLayout
        addRoutesButtons(buttonContainer)
        return root
    }


    private fun addRoutesButtons(bttnContainer: LinearLayout) {
        db.getCurrentUserRoutes { routes ->
            routes?.forEach { route ->
                val routeBtn = Button(context)
                val nameRoute = route["title"] as String
                routeBtn.text = "Este es el titulo: $nameRoute"//routeData["description"] as String
                routeBtn.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)

                routeBtn.setOnClickListener{
                    val intent = Intent(context, ShowRoute :: class.java).apply {
                        putExtra("routeData",  route as Serializable)
                    }
                    startActivity(intent)
                }

                bttnContainer.addView(routeBtn)
            }
            val btnNewRoute = Button(context)
            btnNewRoute.text = "Nueva ruta"
            btnNewRoute.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            btnNewRoute.setOnClickListener {
                val intent = Intent(context, EditRoute :: class.java).apply {
                    posicionarMapa()
                    putExtra("newRoute", true)
                }
                startActivity(intent)
            }
            bttnContainer.addView(btnNewRoute)
        }
    }
    private fun posicionarMapa() {
        GestionarPermisos.requestLocationPermissions(this.requireActivity())
        obtencionLocalizacion.localizacion(this.requireActivity())
            .addOnSuccessListener { location ->
                location?.let {
                        loc->
                    HomeActivity.lastMapPosition = LatLng(loc.latitude, loc.longitude)
                }
            }
    }
}