package com.example.reimbifyapp.auth.ui.component

import android.content.Context
import android.graphics.Color
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatEditText
import com.example.reimbifyapp.R

class CustomOTPEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val otpBoxes = ArrayList<EditText>()
    private val boxCount = 6

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER
        for (i in 0 until boxCount) {
            val box = createOtpBox()
            otpBoxes.add(box)
            addView(box)

            box.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (!s.isNullOrEmpty() && i < boxCount - 1) {
                        otpBoxes[i + 1].requestFocus()
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
            })

            box.setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN && box.text.isNullOrEmpty() && i > 0) {
                    otpBoxes[i - 1].requestFocus()
                    otpBoxes[i - 1].setText("")
                }
                false
            }
        }
    }

    private fun createOtpBox(): EditText {
        val box = AppCompatEditText(context)
        val size = resources.getDimensionPixelSize(R.dimen.otp_box_size)
        val margin = resources.getDimensionPixelSize(R.dimen.otp_box_margin)

        Log.d("CustomOTPEditText", "Box size: $size, margin: $margin")

        box.layoutParams = LayoutParams(size, size).apply {
            setMargins(margin, margin, margin, margin)
        }
        box.gravity = Gravity.CENTER
        box.textSize = 18f
        box.setBackgroundResource(R.drawable.otp_box_background)
        box.inputType = InputType.TYPE_CLASS_NUMBER
        box.filters = arrayOf(InputFilter.LengthFilter(1))
        box.isSingleLine = true
        box.setTextColor(Color.BLACK)
        box.setHintTextColor(Color.GRAY)

        box.alpha = 1f
        box.visibility = VISIBLE

        return box
    }

    fun getOtp(): String {
        return otpBoxes.joinToString("") { it.text.toString() }
    }

    fun validateOtp(): Boolean {
        for (box in otpBoxes) {
            if (box.text.isNullOrEmpty()) {
                box.setBackgroundColor(Color.RED)
                return false
            } else {
                box.setBackgroundResource(R.drawable.otp_box_background)
            }
        }
        return true
    }
}