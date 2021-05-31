package com.jeva.jeva.home.tabs

import android.app.Activity
import android.content.Context
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.jeva.jeva.GestionarPermisos
import com.jeva.jeva.R
import com.jeva.jeva.home.LocaleHelper
import com.jeva.jeva.home.PopUpActivity

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
                        GestionarPermisos.requestRWStoragePermissions(activity)
                        LocaleHelper.setLocale(context,"es")
                        activity.recreate()
                    }
                }

                R.id.set_English -> {
                    if (!item.isChecked) {
                        item.isChecked = true
                        GestionarPermisos.requestRWStoragePermissions(activity)
                        LocaleHelper.setLocale(context,"en")
                        activity.recreate()
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

    }
}