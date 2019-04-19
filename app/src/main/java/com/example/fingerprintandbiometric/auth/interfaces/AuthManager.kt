package com.example.fingerprintandbiometric.auth.interfaces

interface AuthManager {
    fun authenticate()
    fun attachAuthListener(authenticationListener: AuthenticationListener)
}