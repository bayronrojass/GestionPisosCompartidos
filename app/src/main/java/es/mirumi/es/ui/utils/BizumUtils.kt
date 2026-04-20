package es.mirumi.es.utils

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
        val packageManager = context.packageManager
        var appAbierta = false

        for ((paquete, nombreBanco) in bancosSoportados) {
            val intent = packageManager.getLaunchIntentForPackage(paquete)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.data = Uri.parse("bizum://pay?phone=$telefonoReceptor&amount=$cantidad")
                context.startActivity(intent)
                Toast.makeText(context, "Abriendo $nombreBanco para pagar $cantidad€", Toast.LENGTH_SHORT).show()
                appAbierta = true
                break
            }
        }

        if (!appAbierta) {
            try {
                val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse("bizum://"))
                context.startActivity(fallbackIntent)
            } catch (e: Exception) {
                Toast.makeText(context, "Abre tu app bancaria para enviar el Bizum", Toast.LENGTH_LONG).show()
            }
        }
    }
}
