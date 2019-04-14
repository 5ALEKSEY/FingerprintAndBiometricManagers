package com.example.fingerprintandbiometric

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import com.example.fingerprintandbiometric.auth.exceptions.AuthManagerProvideException
import com.example.fingerprintandbiometric.auth.interfaces.AuthManager
import com.example.fingerprintandbiometric.auth.interfaces.AuthenticationListener
import com.example.fingerprintandbiometric.auth.provider.AuthManagerProvider
import com.example.fingerprintandbiometric.extensions.FingerprintState
import com.example.fingerprintandbiometric.extensions.notifyUser

class MainActivity : AppCompatActivity(), AuthenticationListener {

    private lateinit var mAuthManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // authenticate with button click
        findViewById<Button>(R.id.btn_auth_action).setOnClickListener {
            mAuthManager.authenticate()
        }
    }

    override fun onResume() {
        super.onResume()
        initFingerprintLogic()
    }

    override fun onAuthSuccess() {
        notifyUser("onAuthSuccess")
    }

    override fun onAuthFailed() {
        notifyUser("onAuthFailed")
    }

    override fun onAuthCancel() {
        notifyUser("onAuthCancel")
    }

    private fun initFingerprintLogic() {
        try {
            // init AuthManager
            mAuthManager = AuthManagerProvider.provideAuthManager(this, this)
        } catch (e: AuthManagerProvideException) {
            e.printStackTrace()
            if (e.fingerprintState == FingerprintState.NO_ENROLLED_FINGERPRINTS) {
                // open security settings for add fingerprint
                openDeviceSecuritySettings()
            }
            notifyUser(e.fingerprintState.getStateMessage())
        }
    }

    private fun openDeviceSecuritySettings() {
        Intent(Settings.ACTION_SECURITY_SETTINGS).run {
            this.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(this)
        }
    }
}
