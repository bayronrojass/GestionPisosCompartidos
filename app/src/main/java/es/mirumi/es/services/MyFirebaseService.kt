package es.mirumi.es.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import es.mirumi.es.R
import es.mirumi.es.data.remote.NetworkModule
import es.mirumi.es.data.repository.repositories.RepositoryUsuario
import androidx.core.content.edit

class MyFirebaseMessagingService : FirebaseMessagingService() {
    val repository: RepositoryUsuario = RepositoryUsuario(NetworkModule.usuarioApiService)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(
            "FCM",
            "Nuevo token generadoV" +
                " $token",
        )

        guardarTokenEnPreferencias(token)
    }

    private fun guardarTokenEnPreferencias(token: String) {
        val sharedPreferences = getSharedPreferences("fcm_prefs", MODE_PRIVATE)
        sharedPreferences.edit { putString("fcm_token", token) }
        Log.d("FCM", "Token guardado en SharedPreferences.")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        remoteMessage.notification?.let {
            val titulo = it.title ?: "Notificación"
            val cuerpo = it.body ?: "Tienes un mensaje nuevo"

            mostrarNotificacion(titulo, cuerpo)
        }
    }

    private fun mostrarNotificacion(
        titulo: String,
        cuerpo: String,
    ) {
        val channelId = "mirumi_notifications"

        val builder =
            NotificationCompat
                .Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(titulo)
                .setContentText(cuerpo)
                .setAutoCancel(true)

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val channel =
            NotificationChannel(
                channelId,
                "Notificaciones Generales",
                NotificationManager.IMPORTANCE_DEFAULT,
            )
        manager.createNotificationChannel(channel)

        manager.notify(0, builder.build())
    }
}
