package com.example.dozziehotel.ui.alarm

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.example.dozziehotel.databinding.ActivityAlarmBinding
import com.example.dozziehotel.utils.AlarmService

class AlarmActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlarmBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Cấu hình để "đánh thức" điện thoại và hiện đè màn hình khóa
        setupLockScreenVisibility()

        binding = ActivityAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 2. Vô hiệu hóa nút Back theo chuẩn AndroidX mới
        disableBackPressed()

        // 3. Nhận Room ID từ Server gửi qua Service
        val roomId = intent.getStringExtra("ROOM_ID")
        if (!roomId.isNullOrBlank()) {
            binding.tvAlarmMessage.text = "Phòng $roomId của bạn còn 10 phút!"
        }

        // 4. Xử lý nút tắt báo thức
        binding.btnStopAlarm.setOnClickListener {
            // 1. Dừng Service (Tắt MediaPlayer và Vibrator thủ công trong app)
            val serviceIntent = Intent(this, AlarmService::class.java)
            stopService(serviceIntent)

            // 2. Xóa Notification (Tắt âm thanh/rung do OS đang phát)
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(1001) // ID phải khớp với ID lúc notify bên FCM Service

            finish()
        }
    }

    private fun setupLockScreenVisibility() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
    }

    /**
     * Sử dụng OnBackPressedDispatcher để chặn nút Back đúng chuẩn.
     * Khi callback này enabled = true, hệ thống sẽ không thực hiện hành động Back mặc định.
     */
    private fun disableBackPressed() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Không làm gì cả để chặn nút Back/cử chỉ vuốt Back
                // Buộc người dùng phải nhấn nút "Tắt báo thức" trên giao diện
            }
        })
    }
}