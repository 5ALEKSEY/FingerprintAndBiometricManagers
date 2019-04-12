package com.example.fingerprintandbiometric.auth.fingerprint.dialog

import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.os.CancellationSignal
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.airbnb.lottie.LottieAnimationView
import com.example.fingerprintandbiometric.R

@RequiresApi(Build.VERSION_CODES.M)
class FingerprintUiHelper internal constructor(private val fingerprintMgr: FingerprintManager,
                                               private val imageView: ImageView,
                                               private val animationView: LottieAnimationView,
                                               private val errorTextView: TextView,
                                               private val callback: Callback) :
    FingerprintManager.AuthenticationCallback() {

    private var cancellationSignal: CancellationSignal? = null
    private var selfCancelled = false

    val isFingerprintAuthAvailable: Boolean
        get() = fingerprintMgr.isHardwareDetected && fingerprintMgr.hasEnrolledFingerprints()

    private val resetErrorTextRunnable = Runnable {
        animationView.visibility = View.INVISIBLE
        imageView.visibility = View.VISIBLE
        errorTextView.run {
            setTextColor(errorTextView.resources.getColor(R.color.gray, null))
            text = "Touch for unlock"
        }
    }

    fun startListening(cryptoObject: FingerprintManager.CryptoObject) {
        if (!isFingerprintAuthAvailable) return
        cancellationSignal = CancellationSignal()
        selfCancelled = false
        fingerprintMgr.authenticate(cryptoObject, cancellationSignal, 0, this, null)
        errorTextView.post(resetErrorTextRunnable)
    }

    fun stopListening() {
        cancellationSignal?.also {
            selfCancelled = true
            it.cancel()
        }
        cancellationSignal = null
    }

    override fun onAuthenticationError(errMsgId: Int, errString: CharSequence) {
        if (!selfCancelled) {
            showError(errString, false)
        }
        callback.onError(errMsgId)
    }

    override fun onAuthenticationHelp(helpMsgId: Int, helpString: CharSequence) =
        showError(helpString)

    override fun onAuthenticationFailed() =
        showError("Failed")

    override fun onAuthenticationSucceeded(result: FingerprintManager.AuthenticationResult) {
        errorTextView.run {
            removeCallbacks(resetErrorTextRunnable)
            setTextColor(errorTextView.resources.getColor(R.color.fingerprint, null))
            text = "Success"
        }
        imageView.visibility = View.GONE
        animationView.run {
            visibility = View.VISIBLE
            setAnimation("fingerprint_success_animation.json")
            playAnimation()
            postDelayed({ callback.onAuthenticated() }, SUCCESS_DELAY_MILLIS)
        }
    }

    fun showError(error: CharSequence, isErrorResetNeeded: Boolean = true) {
        imageView.visibility = View.GONE
        animationView.visibility = View.VISIBLE
        animationView.setAnimation("fingerprint_failed_animation.json")
        animationView.playAnimation()
        errorTextView.run {
            text = error
            setTextColor(errorTextView.resources.getColor(R.color.red, null))
            removeCallbacks(resetErrorTextRunnable)
            if (isErrorResetNeeded) postDelayed(resetErrorTextRunnable, ERROR_TIMEOUT_MILLIS)
        }
    }

    interface Callback {
        fun onAuthenticated()
        fun onError(errorCode: Int)
    }

    companion object {
        const val ERROR_TIMEOUT_MILLIS: Long = 1600
        const val SUCCESS_DELAY_MILLIS: Long = 1300
    }
}