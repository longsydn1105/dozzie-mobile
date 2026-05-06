// File: app/src/main/java/com/example/dozziehotel/ui/booking/BookingFragment.kt

package com.example.dozziehotel.ui.booking

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.dozziehotel.R
import com.example.dozziehotel.data.local.PreferenceManager
import com.example.dozziehotel.data.model.Room
import com.example.dozziehotel.data.remote.UserDto
import com.example.dozziehotel.data.repository.BookingPreview
import com.example.dozziehotel.databinding.FragmentBookingBinding
import com.example.dozziehotel.ui.bookingimport.RoomBlockAdapter
import com.example.dozziehotel.utils.Resource
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class BookingFragment : Fragment(R.layout.fragment_booking) {
    private var _binding: FragmentBookingBinding? = null
    private val binding get() = _binding!!

    private val roomViewModel: RoomViewModel by viewModel()
    private val bookingViewModel: BookingViewModel by viewModel()

    private val pref: PreferenceManager by inject()
    private var selectedRoom: Room? = null

    private val maleAdapter = RoomBlockAdapter { room ->
        handleRoomSelection(room, isMale = true)
    }

    private val femaleAdapter = RoomBlockAdapter { room ->
        handleRoomSelection(room, isMale = false)
    }

    private fun handleRoomSelection(room: Room?, isMale: Boolean) {
        selectedRoom = room
        if (isMale) {
            femaleAdapter.clearSelection()
        } else {
            maleAdapter.clearSelection()
        }

        // --- LOGIC NÚT ĐẶT PHÒNG ---
        // Nút chỉ sáng lên khi đã chọn phòng
        binding.btnConfirmBooking.apply {
            isEnabled = room != null
            alpha = if (room != null) 1.0f else 0.5f
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentBookingBinding.bind(view)

        val selectedGender = arguments?.getString("booking_gender") ?: ""
        setupRecyclerViews()

        // 1. Hiển thị thông tin (Giữ nguyên logic cũ của ông)
        val date = arguments?.getString("booking_date") ?: ""
        val timeIn = arguments?.getString("booking_time") ?: ""
        val packageName = arguments?.getString("package_name") ?: ""
        val packageHours = arguments?.getInt("package_hours") ?: 0
        val gender = arguments?.getString("booking_gender") ?: "N/A"

        val user = pref.getUser()
        val timeOut = calculateTimeOut(timeIn, packageHours)
        displayInfo(user, date, timeIn, timeOut, packageName, gender)

        // 2. Logic hiển thị Layout theo giới tính
        if (selectedGender == "Nam") {
            binding.layoutFemaleRooms.visibility = View.GONE
            binding.layoutMaleRooms.visibility = View.VISIBLE
        } else {
            binding.layoutMaleRooms.visibility = View.GONE
            binding.layoutFemaleRooms.visibility = View.VISIBLE
        }

        // 3. GỌI API: Room lấy tất cả, Booking lấy các phòng bận
        roomViewModel.fetchAllRooms()
        bookingViewModel.clearOccupiedRooms()
        bookingViewModel.checkRoomAvailability("pending")
        bookingViewModel.checkRoomAvailability("active")

        // 4. Lắng nghe dữ liệu kết hợp phòng và trạng thái occupied
        observeRoomAvailability()

        // 5. Lắng nghe kết quả tạo Booking
        observeBookingResult()

        // 6. Xử lý sự kiện bấm nút Xác nhận đặt phòng
        binding.btnConfirmBooking.setOnClickListener {
            performBooking()
        }
    }

    private fun isTimeOverlapping(userStart: String, userEnd: String, bStart: String, bEnd: String): Boolean {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val s1 = sdf.parse(userStart)?.time ?: 0L
            val e1 = sdf.parse(userEnd)?.time ?: 0L
            val s2 = sdf.parse(bStart)?.time ?: 0L
            val e2 = sdf.parse(bEnd)?.time ?: 0L

            // Logic: A bắt đầu trước khi B kết thúc VÀ A kết thúc sau khi B bắt đầu
            s1 < e2 && e1 > s2
        } catch (e: Exception) {
            false
        }
    }

    // Hàm tính thời gian kết thúc (startTime + hours)
    private fun calculateEndTimeIso(startTimeIso: String, hours: Int): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val date = sdf.parse(startTimeIso) ?: return ""
            val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                time = date
                add(Calendar.HOUR_OF_DAY, hours)
            }
            sdf.format(cal.time)
        } catch (e: Exception) { "" }
    }
    private fun observeRoomAvailability() {
        // Lấy thông tin thời gian khách chọn từ Bundle
        val userStartIso = arguments?.getString("booking_date_iso") ?: ""
        val packageHours = arguments?.getInt("package_hours") ?: 0
        val userEndIso = calculateEndTimeIso(userStartIso, packageHours)

        viewLifecycleOwner.lifecycleScope.launch {
            // Lắng nghe danh sách booking đầy đủ từ ViewModel
            bookingViewModel.allBookings.collect { bookings ->
                // Chỉ lọc ra những ID phòng nào bị trùng giờ thực tế với userStartIso -> userEndIso
                val busyRoomIds = bookings.filter { b ->
                    isTimeOverlapping(
                        userStartIso,
                        userEndIso,
                        b.startTime ?: "",
                        b.endTime ?: ""
                    )
                }.mapNotNull { it.roomId }.toSet()

                // Cập nhật tập hợp ID bị bận vào ViewModel
                bookingViewModel.setOccupiedRoomIds(busyRoomIds)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            // Giữ nguyên logic combine cũ của ông vì nó đang chạy tốt với occupiedRoomIds
            combine(roomViewModel.rooms, bookingViewModel.occupiedRoomIds) { roomResource, occupiedIds ->
                if (roomResource is Resource.Success) {
                    val allRooms = roomResource.data ?: emptyList()
                    allRooms.map { room ->
                        if (occupiedIds.contains(room.id)) {
                            room.copy(status = "occupied")
                        } else {
                            room
                        }
                    }
                } else {
                    null
                }
            }.collect { processedRooms ->
                processedRooms?.let { rooms ->
                    maleAdapter.submitRooms(rooms.filter { it.floor == 3 })
                    femaleAdapter.submitRooms(rooms.filter { it.floor == 2 })
                }
            }
        }
    }

    private fun observeBookingResult() {
        viewLifecycleOwner.lifecycleScope.launch {
            bookingViewModel.createBookingState.collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        Toast.makeText(requireContext(), "Đặt phòng thành công!", Toast.LENGTH_SHORT).show()
                        // Quay về Home và dọn dẹp Stack
                        findNavController().navigate(R.id.action_bookingFragment_to_homeFragment)
                    }
                    is Resource.Error -> {
                        Toast.makeText(requireContext(), resource.message ?: "Lỗi đặt phòng", Toast.LENGTH_SHORT).show()
                    }
                    is Resource.Loading -> {
                        // Có thể thêm ProgressBar nếu cần
                    }
                    else -> {}
                }
            }
        }
    }

    private fun performBooking() {
        val room = selectedRoom ?: return

        // Lấy dữ liệu ID và ISO Time từ arguments (đã được HomeFragment gửi sang)
        val packageId = arguments?.getString("package_id") ?: ""
        val startTimeIso = arguments?.getString("booking_date_iso") ?: ""
        val packagePrice = arguments?.getDouble("package_price") ?: 0.0
        val packageName = arguments?.getString("package_name") ?: ""

        // Log kiểm tra trước khi gọi API
        Log.d("CREATE_BOOKING", "Gửi đi: roomId=${room.id}, packageId=$packageId, startTime=$startTimeIso, packagePrice=$packagePrice, packageName=$packageName")

        if (packageId.isEmpty() || startTimeIso.isEmpty()) {
            Toast.makeText(requireContext(), "Lỗi dữ liệu: Vui lòng quay lại trang chủ chọn lại thời gian", Toast.LENGTH_LONG).show()
            return
        }

        val preview = BookingPreview(
            packageId = packageId,
            packageName = packageName,
            startTimeIso = startTimeIso,
            endTimeIso = "",
            totalPrice = packagePrice
        )

        bookingViewModel.createBooking(room.id ?: "", preview)
    }

    private fun setupRecyclerViews() {
        binding.rvMaleRooms.adapter = maleAdapter
        binding.rvFemaleRooms.adapter = femaleAdapter
    }

    private fun calculateTimeOut(timeIn: String, hours: Int): String {
        return try {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            val date = sdf.parse(timeIn)
            val calendar = Calendar.getInstance().apply {
                if (date != null) time = date
                add(Calendar.HOUR_OF_DAY, hours)
            }
            sdf.format(calendar.time)
        } catch (e: Exception) { "--:--" }
    }

    private fun displayInfo(user: UserDto?, date: String, inT: String, outT: String, pack: String, gender: String) {
        binding.apply {
            rowGender.tvLabel.text = "Giới tính:"
            rowGender.tvValue.text = gender
            rowUser.tvLabel.text = "Khách hàng:"
            rowUser.tvValue.text = user?.fullName
            rowEmail.tvLabel.text = "Email:"
            rowEmail.tvValue.text = user?.email
            rowPhone.tvLabel.text = "Số điện thoại:"
            rowPhone.tvValue.text = user?.phone ?: "0705995857"
            rowDate.tvLabel.text = "Ngày nhận:"
            rowDate.tvValue.text = date
            rowTimeIn.tvLabel.text = "Giờ vào:"
            rowTimeIn.tvValue.text = inT
            rowTimeOut.tvLabel.text = "Giờ ra (dự kiến):"
            rowTimeOut.tvValue.text = outT
            rowPackage.tvLabel.text = "Gói dịch vụ:"
            rowPackage.tvValue.text = pack
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}