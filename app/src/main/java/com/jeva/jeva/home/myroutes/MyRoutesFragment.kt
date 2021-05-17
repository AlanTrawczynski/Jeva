package com.jeva.jeva.home.myroutes

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.jeva.jeva.Database
import com.jeva.jeva.DrawRoute
import com.jeva.jeva.R
import java.io.Serializable

class MyRoutesFragment : Fragment(),Serializable {

    private val db : Database = Database()


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

                routeBtn.text = "Esto son botones de prueba"//routeData["description"] as String
                routeBtn.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)

                routeBtn.setOnClickListener{
                    val intent = Intent(context, DrawRoute :: class.java).apply {
                        putExtra("routeData",  route as Serializable)
                    }
                    startActivity(intent)
                }

                bttnContainer.addView(routeBtn)
            }
        }
    }

}