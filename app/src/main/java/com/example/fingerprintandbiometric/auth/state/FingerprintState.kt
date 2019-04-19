package com.example.fingerprintandbiometric.auth.state

enum class FingerprintState(val stateMessage: String) {
    FINGERPRINT_ALLOW("Everything will be nice :)"),
    VERSION_DOES_NOT_ALLOW("Your device version doesn't allow for fingerprint"),
    DEVICE_HARDWARE_DOES_NOT_ALLOW("Your device hasn't hardware for fingerprint"),
    NO_ENROLLED_FINGERPRINTS("Please, add fingerprint for device in security settings"),
    UNKNOWN_STATE("Ooooops, wtf");
}