package com.jeva.jeva.home

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.jeva.jeva.R
import com.jeva.jeva.database.Database


class GenericPopUp {

    private val db : Database = Database()

    fun showPopupWindow(view: View, layout : Int, f: () -> Unit) {
        val inflater = view.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val popupView: View = inflater.inflate(layout, null)
        val popupWindow = PopupWindow(popupView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, true)
        val closeBtn : Button = popupView.findViewById(R.id.popupBtnClose)
        val applyBtn : Button = popupView.findViewById(R.id.popupBtnApply)

        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0)

        closeBtn.setOnClickListener { popupWindow.dismiss() }
        applyBtn.setOnClickListener {
            getButtonFunction(layout, popupView, popupWindow, view.context)()
        }
    }


    //TODO("comprobaciones de datos")
    private fun getButtonFunction(id : Int, popUpView : View, popupWindow: PopupWindow, context: Context) : () -> Unit{
        when(id){
            R.layout.popup_change_email -> {
                return {
                    val oldEmail = popUpView.findViewById<EditText>(R.id.changeemailOldEmail).text.toString()
                    val pwd = popUpView.findViewById<EditText>(R.id.changepemailPwd).text.toString()
                    val newEmail = popUpView.findViewById<EditText>(R.id.changeemailNewEmail).text.toString()
                    db.updateUserEmail(newEmail, oldEmail,pwd){
                        when(it){
                            Database.UpdateUserEmailStatus.OK ->{
                                popupWindow.dismiss()
                            }
                            Database.UpdateUserEmailStatus.ACCOUNT_DISABLED_OR_DELETED ->{
                                Toast.makeText(context, "Account not found", Toast.LENGTH_SHORT)
                            }
                            Database.UpdateUserEmailStatus.EMAIL_ALREADY_IN_USE ->{
                                Toast.makeText(context, "Email already in use", Toast.LENGTH_SHORT)
                            }
                            Database.UpdateUserEmailStatus.EMAIL_MALFORMED ->{
                                Toast.makeText(context, "Malformed email", Toast.LENGTH_SHORT)
                            }
                            Database.UpdateUserEmailStatus.INVALID_CREDENTIALS ->{
                                Toast.makeText(context, "Invalid credentials", Toast.LENGTH_SHORT)
                            }
                            Database.UpdateUserEmailStatus.SERVER_FAIL ->{
                                Toast.makeText(context, "Error ocurred, please try again", Toast.LENGTH_SHORT)
                            }
                        }
                    }
                }
            }
            R.layout.popup_change_pwd -> {
                return {
                    val email = popUpView.findViewById<EditText>(R.id.changepwdEmail).text.toString()
                    val oldPwd = popUpView.findViewById<EditText>(R.id.changepemailPwd).text.toString()
                    val newPwd = popUpView.findViewById<EditText>(R.id.changepwdNewPwd).text.toString()
                    val newPwdRepeat= popUpView.findViewById<EditText>(R.id.changepwdNewPwdRepeat).text.toString()
                    db.updateUserPassword(newPwd, email, oldPwd){
                        when(it){
                            Database.UpdateUserPasswordStatus.OK ->{
                                popupWindow.dismiss()
                            }
                            Database.UpdateUserPasswordStatus.ACCOUNT_DISABLED_OR_DELETED ->{
                                Toast.makeText(context, "Account not found", Toast.LENGTH_SHORT)
                            }
                            Database.UpdateUserPasswordStatus.INVALID_CREDENTIALS ->{
                                Toast.makeText(context, "Invalid credentials", Toast.LENGTH_SHORT)
                            }
                            Database.UpdateUserPasswordStatus.PASSWORD_WEAK ->{
                                Toast.makeText(context, "Password too weak", Toast.LENGTH_SHORT)
                            }
                            Database.UpdateUserPasswordStatus.SERVER_FAIL ->{
                                Toast.makeText(context, "Error ocurred, please try again", Toast.LENGTH_SHORT)
                            }
                        }
                    }
                }
            }
            else ->{
                return fun(){

                }
            }
        }
    }


}