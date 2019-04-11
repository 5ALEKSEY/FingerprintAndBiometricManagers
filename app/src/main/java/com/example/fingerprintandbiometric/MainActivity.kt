package com.example.fingerprintandbiometric

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import com.example.fingerprintandbiometric.auth.interfaces.AuthManager
import com.example.fingerprintandbiometric.auth.interfaces.AuthenticationListener
import com.example.fingerprintandbiometric.auth.provider.AuthManagerProvider
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), AuthenticationListener {

    private lateinit var mAuthManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // init AuthManager
        mAuthManager = AuthManagerProvider.provideAuthManager(this)
        mAuthManager.setAuthListener(this)
        // authenticate with button click
        authenticateActionButton.setOnClickListener {
            mAuthManager.authenticate()
        }
    }

    override fun onAuthSuccess() {
        Toast.makeText(this, "onAuthSuccess", Toast.LENGTH_SHORT).show()
    }

    override fun onAuthFailed() {
        Toast.makeText(this, "onAuthFailed", Toast.LENGTH_SHORT).show()
    }

    override fun onAuthCancel() {
        Toast.makeText(this, "onAuthCancel", Toast.LENGTH_SHORT).show()
    }
}
