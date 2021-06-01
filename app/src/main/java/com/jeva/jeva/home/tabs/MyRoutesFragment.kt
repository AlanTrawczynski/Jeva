package com.jeva.jeva.home.tabs

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Space
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.storage.StorageReference
import com.jeva.jeva.R
import com.jeva.jeva.database.Database
import com.jeva.jeva.home.EditRouteActivity
import com.jeva.jeva.home.ShowRouteActivity
import kotlinx.android.synthetic.main.fragment_my_routes.*
import java.io.Serializable


class MyRoutesFragment : Fragment(), Serializable {

    private val db : Database = Database()
    private lateinit var root : View
    private var recharge = false



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    activity?.moveTaskToBack(true)
                }
            }
        activity?.onBackPressedDispatcher?.addCallback(this, callback)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        root = inflater.inflate(R.layout.fragment_my_routes, container, false)

        setHasOptionsMenu(true)

        return root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        myRoutesBtnNewRoute.setOnClickListener {
            db.newRoute(listOf()){
                id ->
                if (id != null){
                    val intent = Intent(context, EditRouteActivity :: class.java).apply {
                        putExtra("newRoute", true)
                        putExtra("idRoute", id)
                    }
                    startActivity(intent)
                }
                else{
                    Log.e("ErrorDB", "Ha habido error en la subida")
                }
            }
        }
    }


    override fun onStart() {
        super.onStart()
        val buttonContainer = root.findViewById(R.id.myRoutesButtonContainer) as LinearLayout
        if(recharge) {
            buttonContainer.removeAllViews()
        }
        addRoutesButtons(buttonContainer)
    }


    private fun addRoutesButtons(btnContainer: LinearLayout) {
        db.getCurrentUserRoutes { routes ->
            routes?.forEach { route ->

                val nameRoute = route["title"] as String
                val inflater = view?.context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val cardView : CardView = inflater.inflate(R.layout.popup_cardview_route, null) as CardView
                val textito : TextView = cardView.findViewById(R.id.popupCardViewTitle)
                var routeImage : ImageView = cardView.findViewById(R.id.popupCardViewImage)
                val espacio = Space(context)


                setImageFromDB(route["id"] as String, routeImage)
                textito.text = nameRoute//routeData["description"] as String

                cardView.setOnClickListener{
                    val intent = Intent(context, ShowRouteActivity :: class.java).apply {
                        putExtra("routeData",  route as Serializable)
                    }
                    startActivity(intent)
                }

                espacio.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 60)

                btnContainer.addView(cardView)
                btnContainer.addView(espacio)

            }
        }
        recharge = true
    }


    private fun setImageFromDB(routeId : String, view : ImageView) {
        val ref: StorageReference = db.getRoutePhotoRef(routeId)
        val req = RequestOptions()
            .placeholder(R.drawable.loading)
            .error(R.drawable.error_image)
        val glide = Glide.with(this).applyDefaultRequestOptions(req)

        view.scaleType = ImageView.ScaleType.CENTER_CROP
        view.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,350)

        ref.downloadUrl
            .addOnSuccessListener { glide.load(it).into(view) }
            .addOnFailureListener { glide.load(R.drawable.error_image).into(view) }
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

}