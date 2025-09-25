package com.example.fridgealert.ui

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.fridgealert.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

object NotificationHelper {

    private const val CHANNEL_ID = "expiry_alerts"
    private const val CHANNEL_NAME = "Fridge Alerts"
    private const val CHANNEL_DESC = "แจ้งเตือนวันหมดอายุของอาหาร"

    /** ✅ สร้าง NotificationChannel (เฉพาะ Android 8+) */
    private fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC
                enableVibration(true)
                enableLights(true)
            }

            val manager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    /** ✅ ฟังก์ชันแสดง Notification */
    fun showNotification(context: Context, id: Int, title: String, message: String) {
        createChannel(context)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notifications) // ต้องมีไฟล์ไอคอนใน res/drawable
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        val nm = NotificationManagerCompat.from(context)

        // ✅ ตรวจสอบ Permission สำหรับ Android 13+
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            nm.notify(id, builder.build())
        }

        logNotificationToFirestore(title, message)
    }

    /** ✅ เก็บประวัติ Notification ลง Firestore */
    private fun logNotificationToFirestore(title: String, message: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        val notificationData = hashMapOf(
            "title" to title,
            "message" to message,
            "timestamp" to Date(),
            "userId" to uid
        )

        db.collection("notifications").add(notificationData)
            .addOnFailureListener { e ->
                e.printStackTrace()
            }
    }
}
