package com.jeva.jeva

import android.content.Context
import android.widget.Toast

class Auth {
    companion object {

        fun isValidEmail(email: String): Boolean {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
        }


        fun isValidPassword(pwd: String): Boolean {
//            return Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{6,}$").matcher(pwd).matches();
            return true
        }


        fun authToast(message: CharSequence, c: Context) {
            Toast.makeText(c, message, Toast.LENGTH_SHORT).show()
        }

    }
}