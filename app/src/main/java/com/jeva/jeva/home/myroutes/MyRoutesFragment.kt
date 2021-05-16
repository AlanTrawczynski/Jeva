package com.jeva.jeva.home.myroutes

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.jeva.jeva.Database
import com.jeva.jeva.DrawRoute
import com.jeva.jeva.R
import java.io.Serializable

class MyRoutesFragment : Fragment(),Serializable {

    private val db : Database = Database()
    private val routesContent = mutableMapOf<String,Map<String, Any>>()


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        val root : View = inflater.inflate(R.layout.fragment_my_routes, container, false)
        var buttonContainer = root.findViewById(R.id.myRoutesButtonContainer) as LinearLayout
        addRoutesButtons(buttonContainer)
        return root
    }

    private fun getRouteSecure(routeId: String, tries: Int,  callback: (Map<String, Any>?) -> Unit) {
        if (tries <= 0) {
            callback(null)
        }   else {
            db.getRouteTask(routeId)
                .addOnSuccessListener { doc ->
                    if (doc != null) {
                        callback(doc.data)
                    } else {
                        callback(null)
                        // remove routeId from user data
                    }
                }
                .addOnFailureListener {
                    getRouteSecure(routeId, tries - 1, callback)
                }
        }
    }

    private fun addRoutesButtons(bttnContainer: LinearLayout){

        var userRoutesIds : List<String> = listOf()

        db.getCurrentUser{ userData ->

            if (userData != null) {

                userRoutesIds = userData["routes"] as List<String>

                for (id in userRoutesIds){

                    getRouteSecure(id, 5){ routeData ->

                        if(routeData != null){

                            routesContent.put(id, routeData)
                            var routeBtn = Button(context)
                            routeBtn.text = "Esto son botones de prueba"//routeData["description"] as String
                            routeBtn.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                            routeBtn.setOnClickListener{

                                val intent = Intent(context, DrawRoute :: class.java).apply {
                                    putExtra("routeData",  routeData as Serializable)
                                }
                                startActivity(intent)

                            }
                            bttnContainer.addView(routeBtn)
                        }
                    }
                }
            }
            else {
                Log.e("Myroutes Error: ","No routes found")
                // error toast || reload ?
            }
        }
    }

}