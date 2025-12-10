package es.mirumi.es.services
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import es.mirumi.es.MainActivity

class MyFirebaseMessagingService : FirebaseMessagingService() {
    // Se llama cuando se genera un nuevo token (ej. al instalar la app)
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "Nuevo token generado: $token")

        // AQUÍ: Llama a una función de tu repositorio para enviar este token a tu Backend REST
        enviarTokenAlServidor(token)
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

        val intent =
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                putExtra("navegar_a", "home")
            }

        val pendingIntent =
            PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE,
            )

        val builder =
            NotificationCompat
                .Builder(this, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info) // Pon aquí tu icono: R.drawable.tu_logo
                .setContentTitle(titulo)
                .setContentText(cuerpo)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(
                    channelId,
                    "Notificaciones Generales", // Nombre visible para el usuario
                    NotificationManager.IMPORTANCE_DEFAULT,
                )
            manager.createNotificationChannel(channel)
        }

        manager.notify(0, builder.build())
    }

    private fun enviarTokenAlServidor(token: String) {
        // TODO: Usa Retrofit, Volley o Ktor para enviar el token a tu API
        // Ejemplo: apiService.registrarToken(UserToken(token))
        Log.d("FCM", "Token listo para enviar al backend: $token")
    }
}
