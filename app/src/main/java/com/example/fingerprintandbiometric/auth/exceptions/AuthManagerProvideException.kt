package com.example.fingerprintandbiometric.auth.exceptions

import com.example.fingerprintandbiometric.auth.state.FingerprintState

class AuthManagerProvideException(val fingerprintState: FingerprintState) : Exception()