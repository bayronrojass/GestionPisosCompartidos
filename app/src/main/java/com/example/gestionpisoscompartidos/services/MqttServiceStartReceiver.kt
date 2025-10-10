package com.example.gestionpisoscompartidos.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class MqttServiceStartReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("MqttReceiver", "Dispositivo reiniciado, iniciando servicio MQTT...")
            val serviceIntent = Intent(context, MqttConnectionManagerService::class.java)

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        }
    }
}
