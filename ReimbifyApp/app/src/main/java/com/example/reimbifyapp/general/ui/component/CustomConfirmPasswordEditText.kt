package com.example.reimbifyapp.general.ui.component

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class CustomConfirmPasswordEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.editTextStyle
) : TextInputEditText(context, attrs, defStyleAttr) {

    private var originalPassword: String? = null

    init {
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                validatePassword(s?.toString() ?: "")
            }
        })
    }

    fun setOriginalPassword(password: String) {
        originalPassword = password
        validatePassword(text?.toString() ?: "")
    }

    private fun validatePassword(confirmPassword: String) {
        val parentLayout = getTextInputLayout()
        if (originalPassword != null && originalPassword != confirmPassword) {
            parentLayout?.error = "Passwords do not match"
        } else {
            parentLayout?.error = null
        }
    }

    private fun getTextInputLayout(): TextInputLayout? {
        return this.parent?.parent as? TextInputLayout
    }
}
