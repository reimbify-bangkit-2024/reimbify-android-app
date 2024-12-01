package com.example.reimbifyapp.auth.ui.component

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Patterns
import androidx.appcompat.widget.AppCompatEditText
import com.example.reimbifyapp.R
import com.google.android.material.textfield.TextInputLayout

class CustomEmailEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.style.CustomEditTextStyle
) : AppCompatEditText(context, attrs, defStyleAttr) {

    init {
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val parentLayout = getTextInputLayout()
                val email = s.toString()
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    parentLayout?.error = "Invalid email format"
                } else {
                    parentLayout?.error = null
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun getTextInputLayout(): TextInputLayout? {
        return this.parent?.parent as? TextInputLayout
    }
}