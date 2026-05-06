// File: app/src/main/java/com/example/dozziehotel/ui/booking/BookingViewModel.kt

package com.example.dozziehotel.ui.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dozziehotel.data.repository.BookingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.dozziehotel.data.remote.BookingResponseData
import com.example.dozziehotel.data.repository.BookingPreview
import com.example.dozziehotel.utils.Resource
import com.example.dozziehotel.data.remote.BookingAvailability

class BookingViewModel(
    private val bookingRepository: BookingRepository
) : ViewModel() {

    // Danh sách gốc chứa thông tin thời gian để Fragment lọc
    private val _allBookings = MutableStateFlow<List<BookingAvailability>>(emptyList())
    val allBookings: StateFlow<List<BookingAvailability>> = _allBookings.asStateFlow()

    private val _occupiedRoomIds = MutableStateFlow<Set<String>>(emptySet())
    val occupiedRoomIds: StateFlow<Set<String>> = _occupiedRoomIds.asStateFlow()

    fun checkRoomAvailability(status: String) {
        viewModelScope.launch {
            try {
                val response = bookingRepository.checkAvailability(status)
                if (response.isSuccessful) {
                    val newList = response.body()?.data ?: emptyList()
                    // Gộp danh sách booking mới vào danh sách hiện tại
                    _allBookings.value = _allBookings.value + newList
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Hàm để Fragment cập nhật danh sách ID sau khi đã lọc theo thời gian
    fun setOccupiedRoomIds(ids: Set<String>) {
        _occupiedRoomIds.value = ids
    }

    private val _createBookingState = MutableStateFlow<Resource<BookingResponseData>>(Resource.Idle())
    val createBookingState: StateFlow<Resource<BookingResponseData>> = _createBookingState.asStateFlow()

    fun createBooking(roomId: String, preview: BookingPreview) {
        viewModelScope.launch {
            _createBookingState.value = Resource.Loading()
            val result = bookingRepository.createBooking(roomId, preview)
            _createBookingState.value = result
        }
    }

    fun clearOccupiedRooms() {
        _allBookings.value = emptyList()
        _occupiedRoomIds.value = emptySet()
    }
}