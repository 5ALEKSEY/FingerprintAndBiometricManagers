package com.example.fingerprintandbiometric.auth.fingerprint

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import com.example.fingerprintandbiometric.auth.fingerprint.dialog.FingerprintAuthDialog
import com.example.fingerprintandbiometric.auth.interfaces.AuthManager
import com.example.fingerprintandbiometric.auth.interfaces.AuthenticationListener

@RequiresApi(Build.VERSION_CODES.M)
class FingerprintAuthManager(private val context: Context) : AuthManager {

    private var mAuthenticationListener: AuthenticationListener? = null

    override fun authenticate() {
        val fragmentManager = getCurrentFragmentManager()
        if (fragmentManager != null) {
            FingerprintAuthDialog().run {
                setAuthListener(mAuthenticationListener!!)
                show(fragmentManager, FingerprintAuthDialog.TAG)
            }
        }
    }

    override fun setAuthListener(authenticationListener: AuthenticationListener) {
        this.mAuthenticationListener = authenticationListener
    }

    private fun getCurrentFragmentManager() =
        when (context) {
            is AppCompatActivity -> context.supportFragmentManager
            is Fragment -> context.childFragmentManager
            else -> null
        }
}