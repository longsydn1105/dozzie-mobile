package com.example.dozziehotel.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.dozziehotel.R
import com.example.dozziehotel.data.local.PreferenceManager
import com.example.dozziehotel.data.model.IntroItem
import com.example.dozziehotel.databinding.FragmentHomeBinding
import com.example.dozziehotel.utils.Resource
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.example.dozziehotel.data.remote.ActiveBooking
import com.example.dozziehotel.data.remote.MyStatusData
import com.example.dozziehotel.data.repository.AuthRepository
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.koin.android.ext.android.inject
import java.util.Calendar
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.Locale

class HomeFragment : Fragment(R.layout.fragment_home) {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val pref: PreferenceManager by inject()
    private val homeViewModel: HomeViewModel by viewModel()
    private val sliderHandler = Handler(Looper.getMainLooper())
    private lateinit var sliderRunnable: Runnable
    private var selectedDateMillis: Long? = null
    private var currentActiveBooking: ActiveBooking? = null
    private val serviceViewModel: ServicePackageViewModel by viewModel()
    private val authRepository: AuthRepository by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        val introList = listOf(
            IntroItem("Nhỏ Gọn &\nRiêng Tư.", "Thế giới riêng của bạn chỉ trong một viên nén.", R.drawable.index_1),
            IntroItem("Tiện Nghi &\nHiện Đại.", "Hệ thống IOT thông minh điều khiển ánh sáng.", R.drawable.index_2),
            IntroItem("An Toàn &\nBảo Mật.", "Khóa kén bằng mã QR riêng biệt 24/7.", R.drawable.index_4)
        )

        val adapter = IntroAdapter(introList) {
            findNavController().navigate(R.id.action_homeFragment_to_bookingFragment)
        }
        binding.vpIntro.adapter = adapter

        setupToolbar()
        setupBookingForm()
        setupAutoSlider(introList.size)
        setup360View()

        observeMyStatus()
        homeViewModel.fetchMyStatus(forceRefresh = true)
    }

    /**
     * Thiết lập Toolbar và menu điều hướng (Thanh toán, Lịch sử, IOT, Đăng xuất).
     */
    private fun setupToolbar() {
        binding.btnMenu.setOnClickListener { view ->
            val wrapper = ContextThemeWrapper(requireContext(), R.style.DozziePopupMenuTheme)
            val popup = PopupMenu(wrapper, view)
            popup.menuInflater.inflate(R.menu.home_menu, popup.menu)

            popup.setOnMenuItemClickListener { item ->
                val navController = findNavController()
                if (navController.currentDestination?.id != R.id.homeFragment) return@setOnMenuItemClickListener false

                when (item.itemId) {
                    R.id.menu_payment -> {
                        navController.navigate(R.id.action_homeFragment_to_invoiceFragment)
                        true
                    }
                    R.id.menu_history -> {
                        navController.navigate(R.id.action_homeFragment_to_myBookingFragment)
                        true
                    }
                    R.id.menu_iot -> {
                        if (currentActiveBooking == null) {
                            Toast.makeText(requireContext(), "Bạn chưa đặt phòng hoặc đơn hàng chưa được kích hoạt!", Toast.LENGTH_LONG).show()
                        } else {
                            val bundle = Bundle().apply {
                                putString("room_id", currentActiveBooking?.roomId)
                                putString("digital_key", currentActiveBooking?.digitalKey)
                                putString("booking_id", currentActiveBooking?.id)
                            }
                            findNavController().navigate(R.id.action_homeFragment_to_controlRoomFragment, bundle)
                        }
                        true
                    }
                    R.id.menu_logout -> {
                        handleLogout()
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }

    /**
     * Khởi tạo WebView hiển thị hình ảnh 360 độ sử dụng Pannellum.
     */
    @SuppressLint("ClickableViewAccessibility", "SetJavaScriptEnabled")
    private fun setup360View() {
        binding.wvPanorama.apply {
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
            settings.apply {
                javaScriptEnabled = true
                allowFileAccess = true
                allowContentAccess = true
                domStorageEnabled = true
                mediaPlaybackRequiresUserGesture = false
                @Suppress("DEPRECATION")
                allowFileAccessFromFileURLs = true
                @Suppress("DEPRECATION")
                allowUniversalAccessFromFileURLs = true
                mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            }
            setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> v.parent.requestDisallowInterceptTouchEvent(true)
                    MotionEvent.ACTION_UP -> v.parent.requestDisallowInterceptTouchEvent(false)
                }
                false
            }
            loadUrl("file:///android_asset/panorama.html")
        }
    }

    /**
     * Xóa dữ liệu phiên đăng nhập và quay về màn hình Login.
     */
    private fun handleLogout() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val fcmToken = try {
                    FirebaseMessaging.getInstance().token.await()
                } catch (e: Exception) {
                    null
                }

                authRepository.logout(fcmToken)

                navigateToLogin()

            } catch (e: Exception) {
                pref.clearData()
                navigateToLogin()
            }
        }
    }

    private fun navigateToLogin() {
        try {
            findNavController().navigate(R.id.action_homeFragment_to_loginFragment)
        } catch (e: Exception) {
            val intent = requireActivity().intent
            requireActivity().finish()
            startActivity(intent)
        }
    }

    /**
     * Tự động chuyển đổi các slide giới thiệu sau một khoảng thời gian.
     * Input: [size] tổng số lượng slide.
     */
    private fun setupAutoSlider(size: Int) {
        sliderRunnable = Runnable {
            val currentItem = binding.vpIntro.currentItem
            val nextItem = if (currentItem == size - 1) 0 else currentItem + 1
            binding.vpIntro.setCurrentItem(nextItem, true)
            sliderHandler.postDelayed(sliderRunnable, 5000)
        }
        sliderHandler.postDelayed(sliderRunnable, 5000)
    }

    /**
     * Thiết lập form chọn ngày, giờ và gói dịch vụ để đặt phòng nhanh.
     */
    @SuppressLint("SetTextI18n")
    private fun setupBookingForm() {
        serviceViewModel.fetchActivePackages()
        observePackages()

        val dateConstraints = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointForward.now())
            .build()

        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Chọn ngày nhận phòng")
            .setCalendarConstraints(dateConstraints)
            .build()

        binding.edtDate.setOnClickListener {
            datePicker.show(parentFragmentManager, "DATE_PICKER")
        }

        datePicker.addOnPositiveButtonClickListener { selection ->
            selectedDateMillis = selection
            binding.edtDate.setText(datePicker.headerText)
        }

        binding.edtTime.setOnClickListener {
            val picker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(Calendar.getInstance().get(Calendar.HOUR_OF_DAY))
                .build()

            picker.show(parentFragmentManager, "TIME_PICKER")

            picker.addOnPositiveButtonClickListener {
                binding.edtTime.setText(String.format("%02d:%02d", picker.hour, picker.minute))
            }
        }
    }

    /**
     * Theo dõi danh sách các gói dịch vụ từ server để hiển thị lên dropdown.
     */
    private fun observePackages() {
        viewLifecycleOwner.lifecycleScope.launch {
            serviceViewModel.packages.collect { resource ->
                when (resource) {
                    is Resource.Success<*> -> {
                        val packages = resource.data ?: emptyList()
                        val packageNames = packages.map { it.getDisplayText() }
                        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, packageNames)
                        binding.autoSelectionPackages.setAdapter(adapter)
                    }
                    is Resource.Error<*> -> {
                        Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
            }
        }
    }

    /**
     * Theo dõi trạng thái đặt phòng của người dùng để cập nhật giao diện nút đặt phòng.
     */
    private fun observeMyStatus() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                homeViewModel.myStatus.collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            val data = resource.data ?: return@collect
                            updateUIByStatus(data)
                        }
                        is Resource.Error -> {
                            binding.btnQuickBooking.alpha = 0.5f
                            binding.btnQuickBooking.text = "Đang kiểm tra trạng thái..."
                        }
                        is Resource.Loading -> {
                            binding.btnQuickBooking.isEnabled = false
                            binding.btnQuickBooking.alpha = 0.7f
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    /**
     * Cập nhật trạng thái nút "Đặt phòng" dựa trên dữ liệu thực tế từ server.
     * Input: [data] thông tin trạng thái đặt phòng hiện tại.
     */
    private fun updateUIByStatus(data: MyStatusData) {
        val canBook = data.canBookNew && data.pendingCount == 0
        currentActiveBooking = data.activeBooking

        binding.btnQuickBooking.apply {
            isEnabled = canBook
            isClickable = canBook
            alpha = if (canBook) 1.0f else 0.5f
            text = when {
                data.pendingCount > 0 -> "Đang có đơn hàng chờ (${data.pendingCount})"
                !data.canBookNew -> "Bạn đang trong phòng"
                else -> "Tiếp tục"
            }

            if (!canBook) {
                setOnClickListener {
                    val message = if (data.pendingCount > 0) {
                        "Bạn có ${data.pendingCount} đơn hàng đang chờ xử lý, vui lòng hoàn tất trước khi đặt mới."
                    } else {
                        "Bạn đang sử dụng phòng, không thể đặt thêm kén khác."
                    }
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                }
            } else {
                setOnClickListener { handleQuickBookingAction() }
            }
        }
    }

    /**
     * Thu thập dữ liệu từ form và chuyển hướng sang màn hình xác nhận đặt phòng.
     */
    private fun handleQuickBookingAction() {
        val selectedText = binding.autoSelectionPackages.text.toString()
        val time = binding.edtTime.text.toString()
        val gender = if (binding.rbMale.isChecked) "Nam" else "Nữ"

        val selectedPackage = (serviceViewModel.packages.value.data)?.find {
            it.getDisplayText() == selectedText
        }

        if (selectedDateMillis == null || time.isEmpty() || selectedPackage == null) {
            Toast.makeText(context, "Vui lòng chọn đầy đủ thông tin", Toast.LENGTH_SHORT).show()
            return
        }

        val isoDateTime = try {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = selectedDateMillis!!
            val timeParts = time.split(":")
            calendar.set(Calendar.HOUR_OF_DAY, timeParts[0].toInt())
            calendar.set(Calendar.MINUTE, timeParts[1].toInt())
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            isoFormat.timeZone = java.util.TimeZone.getTimeZone("UTC")
            isoFormat.format(calendar.time)
        } catch (e: Exception) { "" }

        if (isoDateTime.isEmpty()) {
            Toast.makeText(context, "Lỗi xử lý thời gian!", Toast.LENGTH_SHORT).show()
            return
        }

        val bundle = Bundle().apply {
            putString("booking_date", binding.edtDate.text.toString())
            putString("booking_time", time)
            putString("booking_gender", gender)
            putString("package_id", selectedPackage.id)
            putString("package_name", selectedPackage.name)
            putDouble("package_price", selectedPackage.price ?: 0.0)
            putInt("package_hours", selectedPackage.hours)
            putString("booking_date_iso", isoDateTime)
        }
        findNavController().navigate(R.id.action_homeFragment_to_bookingFragment, bundle)
    }

    override fun onPause() {
        super.onPause()
        sliderHandler.removeCallbacks(sliderRunnable)
    }

    override fun onResume() {
        super.onResume()
        sliderHandler.postDelayed(sliderRunnable, 5000)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        sliderHandler.removeCallbacks(sliderRunnable)
        _binding = null
    }
}
