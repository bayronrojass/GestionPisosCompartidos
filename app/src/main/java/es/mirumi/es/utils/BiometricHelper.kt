package es.mirumi.es.utils

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

object BiometricHelper {
    fun isBiometricAvailable(context: Context): Boolean {
        val biometricManager = BiometricManager.from(context)
        return biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun showBiometricPrompt(
        context: Context,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        val activity =
            context as? FragmentActivity ?: run {
                onError("Contexto inválido para biometría")
                return
            }

        val executor = ContextCompat.getMainExecutor(activity)

        val promptInfo =
            BiometricPrompt.PromptInfo
                .Builder()
                .setTitle("Iniciar sesión en MiRumi")
                .setSubtitle("Usa tu huella para acceder a tu piso")
                .setNegativeButtonText("Usar contraseña")
                .build()

        val biometricPrompt =
            BiometricPrompt(
                activity,
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        onSuccess()
                    }

                    override fun onAuthenticationError(
                        errorCode: Int,
                        errString: CharSequence,
                    ) {
                        super.onAuthenticationError(errorCode, errString)
                        onError(errString.toString())
                    }
                },
            )

        biometricPrompt.authenticate(promptInfo)
    }
}
