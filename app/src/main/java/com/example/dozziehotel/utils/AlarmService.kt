package com.example.dozziehotel.utils

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.dozziehotel.R
import com.example.dozziehotel.ui.alarm.AlarmActivity

class AlarmService : Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val roomId = intent?.getStringExtra("ROOM_ID")
        val channelId = "alarm_channel"

        // 1. Build Notification (ID 1001 trùng với FCM để ghi đè lên nhau)
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Dozzie Hotel")
            .setContentText("Phòng $roomId sắp hết giờ!")
            .setSmallIcon(R.drawable.ic_menu)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(getFullScreenIntent(roomId), true)
            .setOngoing(true) // Không cho vuốt xóa
            .build()

        // 2. Chạy Foreground Service (Bắt buộc để duy trì báo thức khi tắt app)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(1001, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
            } else {
                startForeground(1001, notification)
            }
        } catch (e: Exception) {
            Log.e("AlarmService", "Lỗi startForeground: ${e.message}")
        }

        // 3. Mở AlarmActivity (Hành động bổ sung để hiện UI)
        val activityIntent = Intent(this, AlarmActivity::class.java).apply {
            putExtra("ROOM_ID", roomId)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        startActivity(activityIntent)

        return START_STICKY
    }

    private fun createNotificationChannel(channelId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Báo thức khẩn cấp",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Kênh dùng cho thông báo hết giờ phòng"
                setSound(null, null) // Tắt âm mặc định của thông báo vì đã có MediaPlayer
                enableVibration(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun getFullScreenIntent(roomId: String?): PendingIntent {
        val intent = Intent(this, AlarmActivity::class.java).apply {
            putExtra("ROOM_ID", roomId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    override fun onDestroy() {
        // Không còn mediaPlayer/vibrator để cancel ở đây
        super.onDestroy()
    }

    override fun onBind(intent: Intent?) = null
}
