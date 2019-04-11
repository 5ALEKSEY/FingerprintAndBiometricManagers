package com.example.fingerprintandbiometric.auth.interfaces

interface AuthManager {
    fun authenticate()
    fun setAuthListener(authenticationListener: AuthenticationListener)
}