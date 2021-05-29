package com.fayazmohamed.lysync

import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MessageService : FirebaseMessagingService(){

    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)


        var IS_APP_ALIVE = MainActivity.IS_ACTIVITY_ALIVE

        if(!IS_APP_ALIVE){
            var builder = NotificationCompat.Builder(this, "101")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Test Notificaton")
                .setContentText("Contents")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            with(NotificationManagerCompat.from(this)) {
                notify(System.currentTimeMillis().toInt(), builder.build())
            }
        } else {
            MainActivity.REFETCH_TRIGGER.postValue("REFETCH")
        }

    }

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        Log.d("DEBUG", "onNewToken: TOKEN => " + p0)
    }
}