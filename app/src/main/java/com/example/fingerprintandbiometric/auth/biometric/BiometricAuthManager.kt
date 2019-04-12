package com.example.fingerprintandbiometric.auth.biometric

import android.content.Context
import android.content.DialogInterface
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.CancellationSignal
import androidx.annotation.RequiresApi
import com.example.fingerprintandbiometric.R
import com.example.fingerprintandbiometric.auth.interfaces.AuthManager
import com.example.fingerprintandbiometric.auth.interfaces.AuthenticationListener

@RequiresApi(Build.VERSION_CODES.M)
class BiometricAuthManager(private val context: Context) : AuthManager {

    private var mAuthenticationListener: AuthenticationListener? = null
    private val mBiometricAuthCallback = object : BiometricPrompt.AuthenticationCallback() {

        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            mAuthenticationListener?.onAuthFailed()
        }

        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            mAuthenticationListener?.onAuthSuccess()
        }

        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            mAuthenticationListener?.onAuthFailed()
        }
    }

    override fun authenticate() {
        val biometricPrompt = BiometricPrompt.Builder(context)
            .setTitle(context.getString(R.string.fingerprint_dialog_title_text))
            .setDescription(context.getString(R.string.biometric_dialog_description_text))
            .setNegativeButton(
                context.getString(R.string.cancel_button_text), context.mainExecutor,
                DialogInterface.OnClickListener { _, _ -> mAuthenticationListener?.onAuthCancel() })
            .build()

        biometricPrompt.authenticate(
            getCancellationSignal(),
            context.mainExecutor,
            mBiometricAuthCallback
        )
    }

    override fun setAuthListener(authenticationListener: AuthenticationListener) {
        this.mAuthenticationListener = authenticationListener
    }

    private fun getCancellationSignal(): CancellationSignal {
        // With this cancel signal, we can cancel biometric prompt operation
        val cancellationSignal = CancellationSignal()
        cancellationSignal.setOnCancelListener {
            mAuthenticationListener?.onAuthCancel()
        }
        return cancellationSignal
    }
}