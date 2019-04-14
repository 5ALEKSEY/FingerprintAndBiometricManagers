package com.example.fingerprintandbiometric.auth.fingerprint.dialog

import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.os.CancellationSignal
import androidx.annotation.RequiresApi
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.airbnb.lottie.LottieAnimationView
import com.example.fingerprintandbiometric.R

@RequiresApi(Build.VERSION_CODES.M)
class FingerprintUiHelper internal constructor(private val fingerprintMgr: FingerprintManager,
                                               private val imageView: ImageView,
                                               private val animationView: LottieAnimationView,
                                               private val errorTextView: TextView,
                                               private val callback: FingerprintAuthCallback) :
    FingerprintManager.AuthenticationCallback() {

    companion object {
        private const val SUCCESS_LOTTIE_ANIMATION = "fingerprint_success_animation.json"
        private const val FAILED_LOTTIE_ANIMATION = "fingerprint_failed_animation.json"
        private const val ERROR_TIMEOUT_MILLIS: Long = 1600
        private const val SUCCESS_TIMEOUT_MILLIS: Long = 1300
    }

    interface FingerprintAuthCallback {
        fun onAuthenticated()
        fun onError(errorCode: Int)
    }

    private var mCancellationSignal: CancellationSignal? = null
    private var mIsSelfCancelled = false
    private val mResetErrorTextAction = Runnable {
        animationView.visibility = View.INVISIBLE
        imageView.visibility = View.VISIBLE
        errorTextView.apply {
            setTextColor(errorTextView.resources.getColor(R.color.gray, null))
            text = context.getString(R.string.fingerprint_dialog_status_text)
        }
    }

    fun startListening(cryptoObject: FingerprintManager.CryptoObject) {
        mCancellationSignal = CancellationSignal()
        mIsSelfCancelled = false
        fingerprintMgr.authenticate(cryptoObject, mCancellationSignal, 0, this, null)
        errorTextView.post(mResetErrorTextAction)
    }

    fun stopListening() {
        mCancellationSignal?.also {
            mIsSelfCancelled = true
            it.cancel()
        }
        mCancellationSignal = null
    }

    override fun onAuthenticationError(errMsgId: Int, errString: CharSequence) {
        if (!mIsSelfCancelled) {
            showError(errString, false)
        }
        callback.onError(errMsgId)
    }

    override fun onAuthenticationHelp(helpMsgId: Int, helpString: CharSequence) =
        showError(helpString)

    override fun onAuthenticationFailed() =
        showError(errorTextView.context.getString(R.string.failed_auth_action_text))

    override fun onAuthenticationSucceeded(result: FingerprintManager.AuthenticationResult) {
        errorTextView.apply {
            removeCallbacks(mResetErrorTextAction)
            setTextColor(errorTextView.resources.getColor(R.color.colorPrimary, null))
            text = context.getString(R.string.success_auth_action_text)
        }
        imageView.visibility = View.GONE
        animationView.apply {
            visibility = View.VISIBLE
            setAnimation(SUCCESS_LOTTIE_ANIMATION)
            playAnimation()
            postDelayed({ callback.onAuthenticated() }, SUCCESS_TIMEOUT_MILLIS)
        }
    }

    private fun showError(error: CharSequence, isErrorResetNeeded: Boolean = true) {
        imageView.visibility = View.GONE
        animationView.visibility = View.VISIBLE
        animationView.setAnimation(FAILED_LOTTIE_ANIMATION)
        animationView.playAnimation()
        errorTextView.apply {
            text = error
            setTextColor(errorTextView.resources.getColor(R.color.red, null))
            removeCallbacks(mResetErrorTextAction)
            if (isErrorResetNeeded) postDelayed(mResetErrorTextAction, ERROR_TIMEOUT_MILLIS)
        }
    }
}