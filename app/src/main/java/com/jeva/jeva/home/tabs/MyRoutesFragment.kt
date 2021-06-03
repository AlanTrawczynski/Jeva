package com.jeva.jeva.home.tabs

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
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
                val routeTitle = route["title"] as String
                val routeDescription = route["description"] as String

                val inflater = view?.context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val card : CardView = inflater.inflate(R.layout.cardview_route, null) as CardView
                val showBtn: TextView = card.findViewById(R.id.popupCardViewLinkShow)
                val editBtn: TextView = card.findViewById(R.id.popupCardViewLinkEdit)
                val cardImg : ImageView = card.findViewById(R.id.popupCardViewImage)
                val cardTitle : TextView = card.findViewById(R.id.popupCardViewTitle)
                val cardDescription : TextView = card.findViewById(R.id.popupCardViewDescription)
                val space = Space(context)

                setImageFromDB(route["id"] as String, cardImg)
                cardTitle.text = if (routeTitle != "") routeTitle else getString(R.string.no_title)

                if (routeDescription != "") {
                    cardDescription.text = routeDescription.replace(System.lineSeparator(), " ")
                }   else {
                    cardDescription.visibility = View.GONE
                }

                showBtn.setOnClickListener {
                    val intent = Intent(context, ShowRouteActivity:: class.java).apply {
                        putExtra("routeData", route as Serializable)
                    }
                    startActivity(intent)
                }

                editBtn.setOnClickListener {
                    val intent = Intent(context, EditRouteActivity:: class.java).apply {
                        putExtra("routeData", route as Serializable)
                        putExtra("newRoute", false)
                    }
                    startActivity(intent)
                }

                space.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 60)

                btnContainer.addView(card)

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