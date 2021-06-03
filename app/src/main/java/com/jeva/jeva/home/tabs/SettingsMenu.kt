package com.jeva.jeva.home.tabs

import android.app.Activity
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.res.Configuration
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.jeva.jeva.R
import com.jeva.jeva.home.GenericPopUp
import java.util.*
import kotlinx.android.synthetic.main.popup_change_email.*


class SettingsMenu {

    companion object {

        fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
            inflater.inflate(R.menu.settings_menu,menu)
            if(Locale.getDefault().language == "es"){
                menu.findItem(R.id.set_Spanish).isChecked = true
            }else{
                menu.findItem(R.id.set_English).isChecked = true

            }
        }


        fun onOptionsItemSelected(item: MenuItem, view: View, activity: Activity, context: Context) {
            val popup = GenericPopUp()
            when(item.itemId) {

                R.id.set_Spanish -> {
                    if(!item.isChecked) {
                        item.isChecked = true
                        setLanguageForApp("es",context,activity)
                    }
                }

                R.id.set_English -> {
                    if (!item.isChecked) {
                        item.isChecked = true
                        setLanguageForApp("en",context,activity)
                    }
                }

                R.id.sign_out -> {
                    Firebase.auth.signOut()
                    activity.finish()
                }

                R.id.change_email -> {
                    popup.showPopupWindow(view, R.layout.popup_change_email) {
                        Log.d("ei", "email")
                    }
                }

                R.id.change_pwd -> {
                    popup.showPopupWindow(view, R.layout.popup_change_pwd) {
                        Log.d("ei", "pwd")
                    }
                }
            }
        }

        fun setLanguageForApp(languageToLoad: String, context: Context, activity: Activity) {
            val locale = Locale(languageToLoad)
            Locale.setDefault(locale)

            val config = Configuration()
            config.setLocale(locale)
            context.resources.updateConfiguration(
                config,
                context.resources.displayMetrics
            )

            context.getSharedPreferences("GENERAL_STORAGE", MODE_PRIVATE)
                .edit()
                .putString("KEY_USER_LANGUAGE", languageToLoad)
                .apply()

            activity.recreate()
        }

    }
}