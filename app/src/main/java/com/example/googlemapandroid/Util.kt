package com.example.googlemapandroid

import android.content.Context
import android.widget.Toast

fun Context.toast(message: String) {
    Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).apply {
        show()
    }
}