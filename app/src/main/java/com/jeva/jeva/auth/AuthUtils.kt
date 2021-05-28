package com.jeva.jeva.auth

import android.content.Context
import android.widget.Toast
import java.util.regex.Pattern

class AuthUtils {
    companion object {

        fun isValidEmail(email: String) : Boolean {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
        }

        fun isValidPassword(pwd: String) : Boolean {
            return pwd.length >= 6
        }

        fun isValidName(name: String) : Boolean {
            return Pattern.compile("^[a-zA-Z ]{4,25}\$").matcher(name).matches()
        }

        fun isValidUsername(username: String) : Boolean {
            return Pattern.compile("^[\\w._-]{4,15}\$").matcher(username).matches()
        }

        fun authToast(message: CharSequence, c: Context) {
            Toast.makeText(c, message, Toast.LENGTH_SHORT).show()
        }

    }
}