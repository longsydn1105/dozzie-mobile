package com.example.dozziehotel.ui.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dozziehotel.data.remote.BookingNestedDto
import com.example.dozziehotel.data.repository.BookingRepository
import com.example.dozziehotel.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MyBookingViewModel(private val repository: BookingRepository) : ViewModel() {
    private val _bookings = MutableStateFlow<Resource<List<BookingNestedDto>>>(Resource.Loading())
    val bookings: StateFlow<Resource<List<BookingNestedDto>>> = _bookings

    fun fetchMyBookings() {
        viewModelScope.launch {
            _bookings.value = Resource.Loading()
            _bookings.value = repository.getMyBookings()
        }
    }
}