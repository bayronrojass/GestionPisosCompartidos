package com.example.gestionpisoscompartidos.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.gestionpisoscompartidos.R
import info.mqtt.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import kotlin.random.Random

class MqttConnectionManagerService : Service() {
    private lateinit var mqttClient: MqttAndroidClient
    private val brokerUrl = "tcp://192.168.1.131:1883"
    private val clientId = MqttClient.generateClientId()
    private var manager: NotificationManager? = null

    override fun onCreate() {
        super.onCreate()
        manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        Log.d("MqttService", "Servicio MQTT iniciado")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "mqtt_service_channel"
            val channel =
                NotificationChannel(
                    channelId,
                    "MiRumi",
                    NotificationManager.IMPORTANCE_LOW,
                )

            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)

            val notification =
                NotificationCompat
                    .Builder(this, channelId)
                    .setContentTitle("MQTT activo")
                    .setContentText("Conectando al broker MQTT…")
                    .setSmallIcon(android.R.drawable.stat_sys_download_done)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .build()

            startForeground(1, notification)
        }

        mqttClient = MqttAndroidClient(applicationContext, brokerUrl, clientId)

        mqttClient.setCallback(
            object : MqttCallbackExtended {
                override fun connectComplete(
                    reconnect: Boolean,
                    serverURI: String?,
                ) {
                    Log.d("MqttService", "Conectado a MQTT ($serverURI)")
                    mqttClient.subscribe("test", 1)
                }

                override fun connectionLost(cause: Throwable?) {
                    Log.e("MqttService", "Conexión perdida: ${cause?.message}")
                }

                override fun messageArrived(
                    topic: String?,
                    message: MqttMessage?,
                ) {
                    Log.d("MqttService", "Mensaje recibido: $message")
                    mostrarNotificacion(message.toString())
                }

                override fun deliveryComplete(token: IMqttDeliveryToken?) {}
            },
        )

        val options =
            MqttConnectOptions().apply {
                isAutomaticReconnect = true
                isCleanSession = false
            }

        mqttClient.connect(
            options,
            null,
            object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d("MqttService", "Conexión MQTT exitosa")
                }

                override fun onFailure(
                    asyncActionToken: IMqttToken?,
                    exception: Throwable?,
                ) {
                    Log.e("MqttService", "Error al conectar: ${exception?.message}")
                }
            },
        )
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int = START_STICKY

    override fun onDestroy() {
        super.onDestroy()
        mqttClient.unregisterResources()
        mqttClient.close()
        Log.d("MqttService", "Servicio MQTT detenido")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun mostrarNotificacion(texto: String) {
        val channelId = "mqtt_service_channel"

        val notification =
            NotificationCompat
                .Builder(this, channelId)
                .setContentTitle("MiRumi")
                .setContentText(texto)
                .setSmallIcon(android.R.drawable.stat_notify_chat)
                .setAutoCancel(true)
                .build()
        manager!!.notify(Random.nextInt(), notification)
    }
}
