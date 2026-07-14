package es.mirumi.es.ui.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object BizumUtils {
    private val bancosSoportados =
        listOf(
            "com.bbva.bbvacontigo" to "BBVA",
            "es.bancosantander.apps" to "Santander",
            "es.caixabank.pb" to "CaixaBank",
            "com.ing.ingdirect" to "ING",
            "es.bancsabadell.app.particulares" to "Sabadell",
        )

    fun abrirAppBancariaParaBizum(
        context: Context,
        telefonoReceptor: String?,
        cantidad: Double,
    ) {
        ejecutarIntentBizum(context, telefonoReceptor, cantidad, esSolicitud = false)
    }

    fun solicitarBizum(
        context: Context,
        telefonoDeudor: String?,
        cantidad: Double,
    ) {
        ejecutarIntentBizum(context, telefonoDeudor, cantidad, esSolicitud = true)
    }

    private fun ejecutarIntentBizum(
        context: Context,
        telefono: String?,
        cantidad: Double,
        esSolicitud: Boolean,
    ) {
        val packageManager = context.packageManager
        var appAbierta = false

        val accionUri = if (esSolicitud) "request" else "pay"
        val phoneParam = telefono ?: ""
        val uriBizum = Uri.parse("bizum://$accionUri?phone=$phoneParam&amount=$cantidad")

        for ((paquete, nombreBanco) in bancosSoportados) {
            // Comprobamos si la app de este banco está instalada
            val intentLauncher = packageManager.getLaunchIntentForPackage(paquete)
            if (intentLauncher != null) {
                // Intentamos primero mandar la cantidad y número directos
                val deepLinkIntent = Intent(Intent.ACTION_VIEW, uriBizum)
                deepLinkIntent.setPackage(paquete)
                deepLinkIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                if (deepLinkIntent.resolveActivity(packageManager) != null) {
                    context.startActivity(deepLinkIntent)
                } else {
                    // Si el banco no admite inyección directa, abrimos la app del banco
                    intentLauncher.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intentLauncher)
                }

                val msg =
                    if (esSolicitud) {
                        "Abriendo $nombreBanco para solicitar $cantidad€"
                    } else {
                        "Abriendo $nombreBanco para pagar $cantidad€"
                    }
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                appAbierta = true
                break
            }
        }

        if (!appAbierta) {
            Toast
                .makeText(
                    context,
                    "No se encontró ninguna app bancaria compatible instalada",
                    Toast.LENGTH_LONG,
                ).show()
        }
    }
}
