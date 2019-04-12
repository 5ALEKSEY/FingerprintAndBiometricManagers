package com.example.fingerprintandbiometric.auth.fingerprint.dialog

import android.content.Context
import android.content.DialogInterface
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
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
    FingerprintUiHelper.Callback {

    companion object {
        const val TAG = "FingerprintAuthDialog"
        const val ANDROID_KEY_STORE = "AndroidKeyStore"
        const val ENCRYPTION_TRANSFORMATION = KeyProperties.KEY_ALGORITHM_AES + "/" +
                KeyProperties.BLOCK_MODE_CBC + "/" +
                KeyProperties.ENCRYPTION_PADDING_PKCS7
    }

    private val BIP_KEY_ALIAS = "test_alias"

    private lateinit var fingerprintContainer: View
    private lateinit var secondDialogButton: Button

    private lateinit var authenticationListener: AuthenticationListener
    private lateinit var cryptoObject: FingerprintManager.CryptoObject
    private lateinit var fingerprintUiHelper: FingerprintUiHelper
    private lateinit var inputMethodManager: InputMethodManager

    private lateinit var keyStore: KeyStore
    private lateinit var keyGenerator: KeyGenerator

    fun setAuthListener(authenticationListener: AuthenticationListener) {
        this.authenticationListener = authenticationListener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Do not create a new Fragment when the Activity is re-created such as orientation changes.
        retainInstance = true
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

        fingerprintContainer = view.findViewById(R.id.fingerprint_container)
        secondDialogButton = view.findViewById(R.id.second_dialog_button)

        secondDialogButton.setOnClickListener {
            dismiss()
        }

        val animationView = view.findViewById<LottieAnimationView>(R.id.fingerprint_animation_view)
        animationView.useHardwareAcceleration(true)

        fingerprintUiHelper = FingerprintUiHelper(
            activity!!.getSystemService(FingerprintManager::class.java),
            view.findViewById(R.id.fingerprint_image_view),
            animationView,
            view.findViewById(R.id.fingerprint_status),
            this
        )

        secondDialogButton.text = "Cancel"
        fingerprintContainer.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        initAndStartFingerprintListening()
    }

    override fun onPause() {
        super.onPause()
        fingerprintUiHelper.stopListening()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        inputMethodManager = context.getSystemService(InputMethodManager::class.java)
    }

    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)
        authenticationListener.onAuthCancel()
    }

    override fun onAuthenticated() {
        if (isResumed) {
            authenticationListener.onAuthSuccess()
            dismiss()
        }
    }

    override fun onError(errorCode: Int) {
        authenticationListener.onAuthFailed()
        fingerprintUiHelper.stopListening()
    }

    private fun initKeyStoreAndKeyGenerator() {
        fun logException(e: Exception) {
            Log.d(TAG, "initKeyStoreAndKeyGenerator: ${e.message}")
            e.printStackTrace()
        }

        try {
            keyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE)

            keyStore.load(null)

            val keyProperties = KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT

            val builder = KeyGenParameterSpec.Builder(BIP_KEY_ALIAS, keyProperties)
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setUserAuthenticationRequired(true)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
            keyGenerator.init(builder.build())
            keyGenerator.generateKey()

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
        if (initCipher()) {
            fingerprintUiHelper.startListening(cryptoObject)
        }
    }

    private fun initCipher(): Boolean {
        return try {
            initKeyStoreAndKeyGenerator()
            keyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
            keyStore.load(null)
            val cipher = Cipher.getInstance(ENCRYPTION_TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, keyStore.getKey(BIP_KEY_ALIAS, null))
            cryptoObject = FingerprintManager.CryptoObject(cipher)
            true
        } catch (e: Exception) {
            Log.d(TAG, "initCipher: ${e.message}")
            false
        }
    }
}