package com.example.fingerprintandbiometric.auth.exceptions

import com.example.fingerprintandbiometric.extensions.FingerprintState
import java.lang.Exception

class AuthManagerProvideException(val fingerprintState: FingerprintState) : Exception()