package com.example.fingerprintandbiometric.extensions

import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast

fun AppCompatActivity.notifyUser(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this.applicationContext, message, duration).show()
}