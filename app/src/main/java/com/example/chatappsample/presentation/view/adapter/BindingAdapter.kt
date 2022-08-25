package com.example.chatappsample.presentation.view.adapter

import android.view.View
import androidx.databinding.BindingAdapter

@BindingAdapter("isVisible")
fun isVisible(view: View, boolean: Boolean) {
    if (boolean) view.visibility = View.VISIBLE
    else view.visibility = View.GONE
}