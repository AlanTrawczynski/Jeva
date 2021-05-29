package com.jeva.jeva.home

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.jeva.jeva.R


class PopUpActivity {


    fun showPopupWindow(view: View?, layout : Int, buttonId : Int, context: Context?) {

        val inflater = view?.context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView: View = inflater.inflate(layout, null)

        val width = LinearLayout.LayoutParams.MATCH_PARENT
        val height = LinearLayout.LayoutParams.MATCH_PARENT

        val focusable = true

        val popupWindow = PopupWindow(popupView, width, height, focusable)

        popupWindow.contentView.setBackgroundColor(Color.parseColor("#80000000"))
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0)

       //Puedes tratar este botón para que haga lo necesario, hablaremos sobre ello
        val buttonEdit : Button = popupView.findViewById(buttonId)
    }
}