package com.jeva.jeva.home.tabs

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.jeva.jeva.*
import com.jeva.jeva.database.Database
import com.jeva.jeva.home.EditRouteActivity
import com.jeva.jeva.home.LocaleHelper
import com.jeva.jeva.home.PopUpActivity
import com.jeva.jeva.home.ShowRouteActivity
import kotlinx.android.synthetic.main.fragment_my_routes.*
import java.io.Serializable
import java.util.*


class MyRoutesFragment : Fragment(),Serializable {

    private val db : Database = Database()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    Log.e("Soy el boton patras", "Toy desactivado")
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val root : View = inflater.inflate(R.layout.fragment_my_routes, container, false)
        val buttonContainer = root.findViewById(R.id.myRoutesButtonContainer) as LinearLayout

        addRoutesButtons(buttonContainer)
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


    private fun addRoutesButtons(btnContainer: LinearLayout) {
        db.getCurrentUserRoutes { routes ->
            routes?.forEach { route ->

                val nameRoute = route["title"] as String
                val cosaQueSeVe = LinearLayout(context)
                val cardView = CardView(requireContext())
                val textito = TextView(context)

                
                cosaQueSeVe.orientation = LinearLayout.VERTICAL
                cosaQueSeVe.addView(loadRouteImageFromDB(R.drawable.error_image, route["id"] as String, ImageView(context)))


                val radius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10f, context?.resources?.displayMetrics)
                cardView.layoutParams =LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 300)
                cardView.radius = radius


                textito.text = "Este es el titulo: $nameRoute"//routeData["description"] as String
                textito.textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5f, context?.resources?.displayMetrics)

                cosaQueSeVe.addView(textito)
                cosaQueSeVe.addView(loadRouteImageFromDB(R.drawable.error_image, route["id"] as String, ImageView(context)))

                cardView.addView(cosaQueSeVe)

                cardView.setOnClickListener{
                    val intent = Intent(context, ShowRouteActivity :: class.java).apply {
                        putExtra("routeData",  route as Serializable)
                    }
                    startActivity(intent)
                }
                /*routeBtn.setOnClickListener{
                    val intent = Intent(context, ShowRouteActivity :: class.java).apply {
                        putExtra("routeData",  route as Serializable)
                    }
                    startActivity(intent)
                }*/

                btnContainer.addView(cardView)
                //btnContainer.addView(routeBtn)
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.settings_menu,menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val popup = PopUpActivity()
        when(item.itemId){

            R.id.set_Spanish -> {
                if(!item.isChecked){
                    item.isChecked = true
                    GestionarPermisos.requestRWStoragePermissions(activity as Activity)
                    LocaleHelper.setLocale(context,"es")
                    activity?.recreate()
                }
            }

            R.id.set_English -> {
                if (!item.isChecked) {
                    item.isChecked = true
                    GestionarPermisos.requestRWStoragePermissions(activity as Activity)
                    LocaleHelper.setLocale(context,"en")
                    activity?.recreate()
                }
            }

            R.id.sign_out ->{
                val auth = Firebase.auth
                auth.signOut()
                activity?.finish()
            }

            R.id.change_email -> {
                popup.showPopupWindow(view, R.layout.popup_change_email, R.id.popUpChangeEmailButton, context)
            }

            R.id.change_pwd -> {
                popup.showPopupWindow(view, R.layout.popup_change_pwd,  R.id.popUpChangePwdButton, context)
            }


        }

        return super.onOptionsItemSelected(item)
    }



    private fun loadRouteImageFromDB(placeholder: Int, routeId : String, view : ImageView) : ImageView {
        val ref: StorageReference = db.getRoutePhotoRef(routeId)

        ref.downloadUrl.addOnSuccessListener {
            Glide.with(requireContext())
                .load(it)
                .apply(
                    RequestOptions()
                        .placeholder(R.drawable.loading)
                        .error(placeholder)
                )
                .into(view)
            view.scaleType = ImageView.ScaleType.CENTER_CROP
            view.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,250)
        }
            .addOnFailureListener {
                Glide.with(requireContext())
                    .load(placeholder)
                    .into(view)
                view.scaleType = ImageView.ScaleType.CENTER_CROP
                view.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,250)
            }
        return view
    }



}