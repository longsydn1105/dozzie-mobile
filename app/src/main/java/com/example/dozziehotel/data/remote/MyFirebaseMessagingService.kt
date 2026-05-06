package com.example.dozziehotel.data.remote

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.dozziehotel.data.local.PreferenceManager
import com.example.dozziehotel.data.repository.AuthRepository
import com.example.dozziehotel.ui.alarm.AlarmActivity
import com.example.dozziehotel.utils.AlarmService
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import com.example.dozziehotel.R

// Sử dụng KoinComponent để có thể inject vào Service (vì Service do OS khởi tạo)
class MyFirebaseMessagingService : FirebaseMessagingService(), KoinComponent {

    private val authRepository: AuthRepository by inject()
    private val pref: PreferenceManager by inject()

    // Tạo một scope riêng cho Service để chạy coroutine
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Được gọi khi Google cấp một token mới hoặc token cũ hết hạn.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM_SERVICE", "Token mới nhận được: $token")

        // Nếu user đã đăng nhập (có token JWT), tự động cập nhật token mới lên server
        val userToken = pref.getToken()
        if (!userToken.isNullOrBlank()) {
            serviceScope.launch {
                val result = authRepository.updateFcmToken(token)
                if (result is com.example.dozziehotel.utils.Resource.Success) {
                    Log.d("FCM_SERVICE", "Cập nhật token lên server thành công")
                } else {
                    Log.e("FCM_SERVICE", "Cập nhật token lên server thất bại: ${result.message}")
                }
            }
        }
    }

    /**
     * Được gọi khi có thông báo gửi đến khi app đang ở foreground (đang mở).
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val data = remoteMessage.data
        val action = data["action"]
        val roomId = data["roomId"]

        if (action == "ALARM_TIMEOUT") {
            startAlarmFlow(roomId)
        }
        Log.d("FCM_SERVICE", "Nhận thông báo từ: ${remoteMessage.from}")
    }

    // File: app/src/main/java/com/example/dozziehotel/data/remote/MyFirebaseMessagingService.kt

    private fun startAlarmFlow(roomId: String?) {
        val channelId = "alarm_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Alarm", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Kênh báo thức khẩn cấp"
                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build()
                setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM), audioAttributes)
                enableVibration(true)
                // Rung 2 hồi: Rung 500, nghỉ 200, rung 500, nghỉ 1000
                vibrationPattern = longArrayOf(0, 500, 200, 500, 1000)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }

        val fullScreenIntent = Intent(this, AlarmActivity::class.java).apply {
            putExtra("ROOM_ID", roomId)
            // Flag quan trọng để Activity nổi lên trên các app khác
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            this, 0, fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_alarm)
            .setContentTitle("Dozzie Hotel - KHẨN CẤP")
            .setContentText("Phòng $roomId sắp hết giờ!")
            .setPriority(NotificationCompat.PRIORITY_MAX) // Mức cao nhất
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true) // Ép hiển thị Activity ngay cả khi app chết
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
            .setVibrate(longArrayOf(0, 500, 200, 500, 1000))
            .setAutoCancel(false)

        notificationManager.notify(1001, notificationBuilder.build())

        // Khởi chạy Service để duy trì tiếng nhạc/rung lặp lại
        val serviceIntent = Intent(this, AlarmService::class.java).apply {
            putExtra("ROOM_ID", roomId)
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
        } catch (e: Exception) {
            Log.e("FCM", "App killed - Hệ thống sẽ dùng fullScreenIntent để mở màn hình")
        }
    }
}