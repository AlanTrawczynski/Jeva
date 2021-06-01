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
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.jeva.jeva.R
import com.jeva.jeva.home.PopUpActivity
import java.util.*


class SettingsMenu {

    companion object {

        fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
            inflater.inflate(R.menu.settings_menu,menu)
        }


        fun onOptionsItemSelected(item: MenuItem, view: View, activity: Activity, context: Context) {
            val popup = PopUpActivity()
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
                    popup.showPopupWindow(view, R.layout.popup_change_email, R.id.borrar_marcador, context)
                }

                R.id.change_pwd -> {
                    popup.showPopupWindow(view, R.layout.popup_change_pwd,  R.id.changepwdBtnChange, context)
                }
            }
        }

        fun setLanguageForApp(languageToLoad: String, context: Context, activity: Activity) {
            val locale = Locale(languageToLoad)
            Locale.setDefault(locale)

            val config = Configuration()
            config.setLocale(locale)
            context.getResources().updateConfiguration(
                config,
                context.getResources().getDisplayMetrics()
            )

            context.getSharedPreferences("GENERAL_STORAGE", MODE_PRIVATE)
                .edit()
                .putString("KEY_USER_LANGUAGE", languageToLoad)
                .apply()

            activity.recreate()
        }

    }
}