package com.example.reimbifyapp.user.ui.component

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import com.example.reimbifyapp.R
import com.google.android.material.textfield.TextInputLayout

class CustomPasswordEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.style.CustomEditTextStyle
) : AppCompatEditText(context, attrs, defStyleAttr) {

    init {
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val parentLayout = getTextInputLayout()
                val password = s.toString()
                if (password.length < 8) {
                    parentLayout?.error = "Password must be at least 8 characters"
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