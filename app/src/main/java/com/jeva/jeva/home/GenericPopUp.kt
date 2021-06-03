package com.jeva.jeva.home

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.jeva.jeva.R
import com.jeva.jeva.auth.AuthUtils
import com.jeva.jeva.database.Database


class GenericPopUp {

    private val db : Database = Database()


    fun showPopupWindow(view: View, layout : Int) {
        val inflater = view.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView: View = inflater.inflate(layout, null)
        val popupWindow = PopupWindow(popupView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, true)
        val closeBtn : Button = popupView.findViewById(R.id.popupBtnClose)
        val applyBtn : Button = popupView.findViewById(R.id.popupBtnApply)

        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0)

        closeBtn.setOnClickListener { popupWindow.dismiss() }
        applyBtn.setOnClickListener {
            getApplyBtnFunction(layout, popupView, popupWindow, view.context)()
        }
    }


    //TODO("comprobaciones de datos")
    private fun getApplyBtnFunction(id : Int, popUpView : View, popupWindow: PopupWindow, context: Context) : () -> Unit {
        when(id) {
            R.layout.popup_change_email -> {
                return {
                    val oldEmail = popUpView.findViewById<EditText>(R.id.changeemailOldEmail).text.toString()
                    val newEmail = popUpView.findViewById<EditText>(R.id.changeemailNewEmail).text.toString()
                    val pwd = popUpView.findViewById<EditText>(R.id.changepemailPwd).text.toString()

                    if (!AuthUtils.isValidEmail(oldEmail)) {
                        showToast(R.string.not_valid_email, context)
                    }
                    else if (!AuthUtils.isValidEmail(newEmail)) {
                        showToast(R.string.not_valid_newEmail, context)
                    }
                    else if (!AuthUtils.isValidPassword(pwd)) {
                        showToast(R.string.not_valid_password, context)
                    }
                    else {
                        db.updateUserEmail(newEmail, oldEmail,pwd) {
                            when(it) {
                                Database.UpdateUserEmailStatus.OK -> {
                                    popupWindow.dismiss()
                                    showToast(R.string.user_data_updated, context)
                                }
                                Database.UpdateUserEmailStatus.ACCOUNT_DISABLED_OR_DELETED -> {
                                    showToast(R.string.account_disabled_or_deleted, context)
                                }
                                Database.UpdateUserEmailStatus.EMAIL_ALREADY_IN_USE -> {
                                    showToast(R.string.email_in_use, context)
                                }
                                Database.UpdateUserEmailStatus.INVALID_CREDENTIALS -> {
                                    showToast(R.string.invalid_credentials, context)
                                }
                                Database.UpdateUserEmailStatus.SERVER_FAIL -> {
                                    showToast(R.string.error_occurred, context)
                                }
                            }
                        }
                    }
                }
            }

            R.layout.popup_change_pwd -> {
                return {
                    val email = popUpView.findViewById<EditText>(R.id.changepwdEmail).text.toString()
                    val oldPwd = popUpView.findViewById<EditText>(R.id.changepwdOldPwd).text.toString()
                    val newPwd = popUpView.findViewById<EditText>(R.id.changepwdNewPwd).text.toString()
                    val newPwdRepeat = popUpView.findViewById<EditText>(R.id.changepwdNewPwdRepeat).text.toString()

                    if (!AuthUtils.isValidEmail(email)) {
                        showToast(R.string.not_valid_email, context)
                    }
                    else if (!AuthUtils.isValidPassword(oldPwd) || !AuthUtils.isValidPassword(newPwd)) {
                        showToast(R.string.not_valid_password, context)
                    }
                    else if (newPwd != newPwdRepeat) {
                        showToast(R.string.passwords_dont_match, context)
                    }
                    else {
                        db.updateUserPassword(newPwd, email, oldPwd) {
                            when(it) {
                                Database.UpdateUserPasswordStatus.OK -> {
                                    popupWindow.dismiss()
                                    showToast(R.string.user_data_updated, context)
                                }
                                Database.UpdateUserPasswordStatus.ACCOUNT_DISABLED_OR_DELETED -> {
                                    showToast(R.string.account_disabled_or_deleted, context)
                                }
                                Database.UpdateUserPasswordStatus.INVALID_CREDENTIALS -> {
                                    showToast(R.string.invalid_credentials, context)
                                }
                                Database.UpdateUserPasswordStatus.SERVER_FAIL -> {
                                    showToast(R.string.error_occurred, context)
                                }
                            }
                        }
                    }
                }
            }

            else -> { return {} }
        }
    }


    private fun showToast(rId: Int, context: Context) {
        Toast.makeText(context, rId, Toast.LENGTH_SHORT).show()
    }

}