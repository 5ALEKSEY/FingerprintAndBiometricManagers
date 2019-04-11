package com.example.fingerprintandbiometric.extensions

import android.Manifest
import android.app.KeyguardManager
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat

enum class FingerprintState(private val stateMessage: String) {
    FINGERPRINT_ALLOW("Everything will be nice :)"),
    VERSION_DOES_NOT_ALLOW("Your device version doesn't allow for fingerprint"),
    DEVICE_HARDWARE_DOES_NOT_ALLOW("Your device hasn't hardware for fingerprint"),
    NO_ENROLLED_FINGERPRINTS("Please, add fingerprint for device in security settings"),
    UNKNOWN_STATE("Ooooops, wtf");

    fun getStateMessage() = stateMessage
}

fun Context.checkDeviceForFingerprintAllow() =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val fingerprintManager = this.getSystemService(Context.FINGERPRINT_SERVICE) as? FingerprintManager

        if (fingerprintManager == null) {
            FingerprintState.UNKNOWN_STATE
        } else if (!fingerprintManager.isHardwareDetected) {
            FingerprintState.DEVICE_HARDWARE_DOES_NOT_ALLOW
        } else if (!fingerprintManager.hasEnrolledFingerprints()) {
            FingerprintState.NO_ENROLLED_FINGERPRINTS
        } else {
            FingerprintState.FINGERPRINT_ALLOW
        }
    } else {
        FingerprintState.VERSION_DOES_NOT_ALLOW
    }


@RequiresApi(Build.VERSION_CODES.M)
fun Context.isBiometricSupported(): Boolean {
    val keyguardManager = this.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager ?: return false
    if (!keyguardManager.isKeyguardSecure) {
        return false
    }
    if (ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.USE_BIOMETRIC
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        return false
    }

    return this.packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)
}