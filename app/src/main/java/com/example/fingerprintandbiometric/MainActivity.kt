package com.example.fingerprintandbiometric

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.fingerprintandbiometric.auth.exceptions.AuthManagerProvideException
import com.example.fingerprintandbiometric.auth.interfaces.AuthManager
import com.example.fingerprintandbiometric.auth.interfaces.AuthenticationListener
import com.example.fingerprintandbiometric.auth.provider.AuthManagerProvider
import com.example.fingerprintandbiometric.extensions.notifyUser
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), AuthenticationListener {

    private lateinit var mAuthManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        try {
            // init AuthManager
            mAuthManager = AuthManagerProvider.provideAuthManager(this, this)
            // authenticate with button click
            authenticateActionButton.setOnClickListener {
                mAuthManager.authenticate()
            }
        } catch (e: AuthManagerProvideException) {
            notifyUser(e.fingerprintState.getStateMessage())
            e.printStackTrace()
        }
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
}
