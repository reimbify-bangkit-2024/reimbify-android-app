package com.example.reimbifyapp.user.utils

import android.view.View
import android.view.ViewGroup

val ViewGroup.children: List<View>
    get() = (0 until childCount).map { getChildAt(it) }
