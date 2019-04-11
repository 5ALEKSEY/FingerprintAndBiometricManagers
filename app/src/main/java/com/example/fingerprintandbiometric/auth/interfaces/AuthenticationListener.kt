package com.example.fingerprintandbiometric.auth.interfaces

interface AuthenticationListener {
    fun onAuthSuccess()
    fun onAuthFailed()
    fun onAuthCancel()
}