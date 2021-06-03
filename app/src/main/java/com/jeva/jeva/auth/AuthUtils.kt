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

        fun authToast(rId: Int, c: Context) {
            Toast.makeText(c, rId, Toast.LENGTH_SHORT).show()
        }

    }
}