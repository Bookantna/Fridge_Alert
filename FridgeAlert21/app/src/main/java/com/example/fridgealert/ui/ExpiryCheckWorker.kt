package com.example.fridgealert.ui


import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.abs

class ExpiryCheckWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }
    private val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    override suspend fun doWork(): Result {
        val uid = auth.currentUser?.uid ?: return Result.success()
        try {

            val snap = db.collection("items")
                .whereEqualTo("userId", uid)
                .get().await()

            val today = LocalDate.now()

            for (doc in snap.documents) {
                val name = doc.getString("name") ?: continue
                val exp = doc.getString("expDate") ?: continue

                val expDate = runCatching { LocalDate.parse(exp, fmt) }.getOrNull() ?: continue
                val days = java.time.Period.between(today, expDate).days

                when {
                    days == 1 -> {
                        // ใกล้หมดอายุ (พรุ่งนี้หมด)
                        NotificationHelper.showNotification(
                            context = applicationContext,
                            id = doc.id.hashCode(),
                            title = "ใกล้หมดอายุ: $name",
                            message = "เหลือ 1 วันจะหมดอายุ รีบนำไปทำอาหารนะ!"
                        )
                    }
                    days <= 0 -> {
                        // หมดอายุแล้ว → แจ้งเตือนทุกวันจนกว่าจะถูกลบ
                        NotificationHelper.showNotification(
                            context = applicationContext,
                            id = doc.id.hashCode(),
                            title = "หมดอายุแล้ว: $name",
                            message = "ควรนำไปทิ้งหรือจัดการให้เรียบร้อย"
                        )
                    }

                }
            }
            return Result.success()
        } catch (e: Exception) {
            return Result.retry()
        }
    }
}
