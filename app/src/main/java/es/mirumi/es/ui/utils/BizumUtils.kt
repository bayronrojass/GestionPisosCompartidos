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

        for ((paquete, nombreBanco) in bancosSoportados) {
            val intent = packageManager.getLaunchIntentForPackage(paquete)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.data = Uri.parse("bizum://$accionUri?phone=$phoneParam&amount=$cantidad")
                context.startActivity(intent)

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
            try {
                val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse("bizum://$accionUri"))
                fallbackIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(fallbackIntent)
            } catch (e: Exception) {
                val msg =
                    if (esSolicitud) {
                        "Abre tu app bancaria para solicitar el Bizum"
                    } else {
                        "Abre tu app bancaria para enviar el Bizum"
                    }
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            }
        }
    }
}
