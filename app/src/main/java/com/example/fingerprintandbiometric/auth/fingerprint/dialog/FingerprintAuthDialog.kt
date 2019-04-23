package com.example.fingerprintandbiometric.auth.fingerprint.dialog

import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.airbnb.lottie.LottieAnimationView
import com.example.fingerprintandbiometric.R
import com.example.fingerprintandbiometric.auth.interfaces.AuthenticationListener
import java.io.IOException
import java.security.InvalidAlgorithmParameterException
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.cert.CertificateException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator

@RequiresApi(Build.VERSION_CODES.M)
class FingerprintAuthDialog : DialogFragment(),
    FingerprintUiHelper.FingerprintUiHelperCallback {

    companion object {
        const val TAG = "FingerprintAuthDialog"
        const val ANDROID_KEY_STORE = "AndroidKeyStore"
        const val ENCRYPTION_TRANSFORMATION = KeyProperties.KEY_ALGORITHM_AES + "/" +
                KeyProperties.BLOCK_MODE_CBC + "/" +
                KeyProperties.ENCRYPTION_PADDING_PKCS7
    }

    // You should use your unique alias for key in key store (for example userId)
    private val BIP_KEY_ALIAS = "test_alias"

    private lateinit var mFingerprintContainer: View
    private lateinit var mCancelActionButton: Button

    private lateinit var mCryptoObject: FingerprintManager.CryptoObject
    private lateinit var mFingerprintUiHelper: FingerprintUiHelper
    private lateinit var mKeyGenerator: KeyGenerator
    private lateinit var mKeyStore: KeyStore

    private var mAuthenticationListener: AuthenticationListener? = null

    fun setAuthListener(authenticationListener: AuthenticationListener) {
        this.mAuthenticationListener = authenticationListener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        retainInstance = true
        isCancelable = false
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_Dialog)
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        dialog?.requestWindowFeature(STYLE_NO_TITLE)
        dialog?.window?.setBackgroundDrawableResource(R.drawable.bg_fingerprint_dialog)
        return inflater.inflate(R.layout.fingerprint_dialog_container, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mFingerprintContainer = view.findViewById(R.id.fingerprint_container)
        mCancelActionButton = view.findViewById(R.id.btn_cancel_action)

        mCancelActionButton.setOnClickListener {
            mFingerprintUiHelper.stopListening()
            mAuthenticationListener?.onAuthCancel()
            dismiss()
        }

        val animationView = view.findViewById<LottieAnimationView>(R.id.fingerprint_animation_view)
        animationView.useHardwareAcceleration(true)

        mFingerprintUiHelper = FingerprintUiHelper(
            activity!!.getSystemService(FingerprintManager::class.java),
            view.findViewById(R.id.fingerprint_image_view),
            animationView,
            view.findViewById(R.id.fingerprint_status),
            this,
            mAuthenticationListener
        )

        mCancelActionButton.text = getString(R.string.cancel_button_text)
        mFingerprintContainer.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        initAndStartFingerprintListening()
    }

    override fun onPause() {
        super.onPause()
        mFingerprintUiHelper.stopListening()
    }

    override fun onAuthenticated() {
        if (isResumed) {
            mAuthenticationListener?.onAuthSuccess()
            dismiss()
        }
    }

    override fun onError() {
        mAuthenticationListener?.onAuthFailed()
        mFingerprintUiHelper.stopListening()
    }

    private fun initKeyStoreAndKeyGenerator() {
        fun logException(e: Exception) {
            Log.d(TAG, "initKeyStoreAndKeyGenerator: ${e.message}")
            e.printStackTrace()
        }

        try {
            mKeyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
            mKeyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE)

            mKeyStore.load(null)

            val keyProperties = KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT

            val builder = KeyGenParameterSpec.Builder(BIP_KEY_ALIAS, keyProperties)
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setUserAuthenticationRequired(true)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
            mKeyGenerator.init(builder.build())
            mKeyGenerator.generateKey()

        } catch (e: KeyStoreException) {
            logException(e)
        } catch (e: IOException) {
            logException(e)
        } catch (e: NoSuchAlgorithmException) {
            logException(e)
        } catch (e: CertificateException) {
            logException(e)
        } catch (e: InvalidAlgorithmParameterException) {
            logException(e)
        }
    }

    private fun initAndStartFingerprintListening() {
        if (isCipherInitSuccess()) {
            mFingerprintUiHelper.startListening(mCryptoObject)
        }
    }

    private fun isCipherInitSuccess(): Boolean {
        return try {
            initKeyStoreAndKeyGenerator()
            mKeyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
            mKeyStore.load(null)
            val cipher = Cipher.getInstance(ENCRYPTION_TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, mKeyStore.getKey(BIP_KEY_ALIAS, null))
            mCryptoObject = FingerprintManager.CryptoObject(cipher)
            true
        } catch (e: Exception) {
            Log.d(TAG, "isCipherInitSuccess: ${e.message}")
            false
        }
    }
}