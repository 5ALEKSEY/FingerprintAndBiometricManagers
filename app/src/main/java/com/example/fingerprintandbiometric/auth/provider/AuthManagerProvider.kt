package com.example.fingerprintandbiometric.auth.provider

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.fingerprintandbiometric.auth.biometric.BiometricAuthManager
import com.example.fingerprintandbiometric.auth.fingerprint.FingerprintAuthManager
import com.example.fingerprintandbiometric.extensions.isBiometricSupported

@RequiresApi(api = Build.VERSION_CODES.M)
class AuthManagerProvider {
    companion object {
        fun provideAuthManager(context: Context) =
            if (Build.VERSION.SDK_INT >= 28 && context.isBiometricSupported())
                BiometricAuthManager(context)
            else
                FingerprintAuthManager(context)
    }
}