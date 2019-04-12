package com.example.fingerprintandbiometric.extensions

import android.app.Activity
import android.widget.Toast

fun Activity.notifyUser(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this.applicationContext, message, duration).show()
}