// File: app/src/main/java/com/example/dozziehotel/ui/control/ControlRoomFragment.kt

package com.example.dozziehotel.ui.control

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.dozziehotel.R
import com.example.dozziehotel.data.remote.IotCommandResponse
import com.example.dozziehotel.databinding.FragmentControlRoomBinding
import com.example.dozziehotel.utils.Resource
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ControlRoomFragment : Fragment(R.layout.fragment_control_room) {
    private lateinit var binding: FragmentControlRoomBinding
    private val viewModel: ControlRoomViewModel by viewModel()
    private val handler = Handler(Looper.getMainLooper())
    private var sosStartTime = 0L
    private val SOS_DURATION = 3000L

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentControlRoomBinding.bind(view)

        val roomId = arguments?.getString("room_id") ?: "Phòng"
        binding.tvRoomLabel.text = "điều khiển phòng $roomId"

        // Các nút bấm thường
        binding.btnOpen.setOnClickListener { viewModel.sendCommand("DOOR_OPEN") }
        binding.btnClose.setOnClickListener { viewModel.sendCommand("DOOR_CLOSE") }

        binding.btnChat.setOnClickListener {
            val bId = arguments?.getString("booking_id")
            val rId = arguments?.getString("room_id")

            if (bId != null) {
                val bundle = Bundle().apply {
                    putString("bookingId", bId)
                    putString("roomId", rId)
                }
                findNavController().navigate(R.id.action_controlRoomFragment_to_chatFragment, bundle)
            } else {
                Toast.makeText(requireContext(), "Không tìm thấy thông tin đặt phòng", Toast.LENGTH_SHORT).show()
            }
        }
        setupCheckout()

        setupSosLongPress()

        observeResult()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupSosLongPress() {
        binding.btnSos.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    sosStartTime = System.currentTimeMillis()
                    // Hiệu ứng phóng to
                    v.animate().scaleX(1.1f).scaleY(1.1f).setDuration(200).start()
                    // Chạy vòng tròn progress
                    ObjectAnimator.ofInt(binding.sosProgress, "progress", 3000)
                        .setDuration(SOS_DURATION)
                        .start()

                    handler.postDelayed({
                        viewModel.handleSosClick()
                        v.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
                    }, SOS_DURATION)
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    handler.removeCallbacksAndMessages(null)
                    // Reset hiệu ứng
                    v.animate().scaleX(1f).scaleY(1f).setDuration(200).start()
                    binding.sosProgress.progress = 0
                    true
                }
                else -> false
            }
        }
    }

    private fun observeResult() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observe kết quả IoT (Mở/đóng cửa)
                launch {
                    viewModel.iotResult.collect { resource ->
                        handleUiState(resource)
                    }
                }

                // Observe kết quả SOS
                launch {
                    viewModel.sosResult.collect { resource ->
                        if (resource is Resource.Success) {
                            Toast.makeText(context, "Đã gửi yêu cầu cứu hộ thành công!", Toast.LENGTH_LONG).show()
                        } else if (resource is Resource.Error) {
                            Toast.makeText(context, "Lỗi SOS: ${resource.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }

                launch {
                    viewModel.checkoutResult.collect { resource ->
                        when (resource) {
                            is Resource.Loading -> {
                                binding.btnCheckout.isEnabled = false
                                binding.btnCheckout.text = "Đang xử lý..."
                            }
                            is Resource.Success -> {
                                Toast.makeText(context, "Trả phòng thành công!", Toast.LENGTH_SHORT).show()
                                // Về Home và refresh lại trạng thái (quan trọng)
                                findNavController().popBackStack(R.id.homeFragment, false)
                            }
                            is Resource.Error -> {
                                binding.btnCheckout.isEnabled = true
                                binding.btnCheckout.text = "TRẢ PHÒNG (CHECKOUT)"
                                Toast.makeText(context, resource.message, Toast.LENGTH_LONG).show()
                                viewModel.resetCheckoutState()
                            }
                            else -> {}
                        }
                    }
                }
            }
        }
    }

    private fun handleUiState(resource: Resource<*>) {
        val isLoading = resource is Resource.Loading
        binding.cardDoor.alpha = if (isLoading) 0.5f else 1.0f
        binding.btnOpen.isEnabled = !isLoading
        binding.btnClose.isEnabled = !isLoading

        if (resource is Resource.Success && resource.data is IotCommandResponse) {
            Toast.makeText(context, resource.data.message, Toast.LENGTH_SHORT).show()
        } else if (resource is Resource.Error) {
            Toast.makeText(context, resource.message, Toast.LENGTH_LONG).show()
        }
    }

    private fun setupCheckout() {
        binding.btnCheckout.setOnClickListener {
            val bookingId = arguments?.getString("booking_id")
            if (bookingId != null) {
                // Hiện confirm dialog trước khi checkout
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Xác nhận trả phòng")
                    .setMessage("Bạn có muốn trả phòng ngay bây giờ không?")
                    .setPositiveButton("Xác nhận") { _, _ ->
                        viewModel.checkout(bookingId)
                    }
                    .setNegativeButton("Hủy", null)
                    .show()
            }
        }
    }
}
