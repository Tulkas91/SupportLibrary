package it.mm.supportlibrary.helper

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.PromptInfo
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import it.mm.supportlibrary.core.utilities.SingleLiveEvent
import java.util.concurrent.Executor

class BiometricAuthenticationHelper(val context: Context) {

    companion object {
        private var executor: Executor? = null
        private var biometricPrompt: BiometricPrompt? = null
        private var promptInfo: PromptInfo? = null

        // LiveData per l'autenticazione
        @JvmStatic
        private val _authenticationResult = SingleLiveEvent<Boolean>()
        @JvmStatic
        val authenticationResult: LiveData<Boolean> get() = _authenticationResult

        @JvmStatic
        fun setPrompt(fragment: Fragment, activity: Activity) {
            executor = ContextCompat.getMainExecutor(activity)
            biometricPrompt = BiometricPrompt(
                fragment,
                executor!!,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        _authenticationResult.emit(false)
                    }

                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        _authenticationResult.emit(true)
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        _authenticationResult.emit(false)
                    }
                })
        }

        @JvmStatic
        fun initBiometricPrompt(
            title: String,
            subtitle: String,
            description: String,
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                promptInfo = PromptInfo.Builder()
                    .setTitle(title)
                    .setSubtitle(subtitle)
                    .setDescription(description)
                    .setAllowedAuthenticators(BiometricManager.Authenticators.DEVICE_CREDENTIAL or BiometricManager.Authenticators.BIOMETRIC_STRONG)
                    .build()
            else
                promptInfo = PromptInfo.Builder()
                    .setTitle(title)
                    .setSubtitle(subtitle)
                    .setDescription(description)
                    .setNegativeButtonText("Annulla")
                    .build()
        }

        @JvmStatic
        fun showBiometricDialog() {
            biometricPrompt!!.authenticate(promptInfo!!)
        }

        @JvmStatic
        fun isBiometricHardWareAvailable(context: Context?): Boolean {
            var result = false
            val biometricManager = BiometricManager.from(
                context!!
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
                    BiometricManager.BIOMETRIC_SUCCESS -> result = true
                    BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> result = false
                    BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> result = false
                    BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> result = false
                    BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> result = true
                    BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> result = true
                    BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> result = false
                }
            } else {
                when (biometricManager.canAuthenticate()) {
                    BiometricManager.BIOMETRIC_SUCCESS -> result = true
                    BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> result = false
                    BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> result = false
                    BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> result = false
                }
            }
            return result
        }

        @JvmStatic
        fun deviceHasPasswordPinLock(context: Context): Boolean {
            val keymgr =
                context.getSystemService(AppCompatActivity.KEYGUARD_SERVICE) as KeyguardManager
            if (keymgr.isKeyguardSecure) return true
            return false
        }

        @JvmStatic
        fun replace_word(pattern: String): String {
            val asterisk_val = StringBuilder()
            for (i in 0 until pattern.length) asterisk_val.append('*')
            return asterisk_val.toString()
        }
    }
}
