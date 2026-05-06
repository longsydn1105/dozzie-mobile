package com.example.dozziehotel.ui.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dozziehotel.data.remote.MyStatusData
import com.example.dozziehotel.data.repository.BookingRepository
import com.example.dozziehotel.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val bookingRepository: BookingRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _myStatus = MutableStateFlow<Resource<MyStatusData>>(Resource.Idle())
    val myStatus: StateFlow<Resource<MyStatusData>> = _myStatus

    private val IS_FETCHED_KEY = "home_status_fetched"

    /**
     * Lấy trạng thái đặt phòng hiện tại của người dùng.
     * Input: [forceRefresh] nếu true sẽ luôn fetch mới từ server.
     */
    fun fetchMyStatus(forceRefresh: Boolean = false) {
        if (!forceRefresh && savedStateHandle.get<Boolean>(IS_FETCHED_KEY) == true
            && _myStatus.value is Resource.Success) return

        viewModelScope.launch {
            _myStatus.value = Resource.Loading()
            val result = bookingRepository.getMyStatus()
            _myStatus.value = result

            if (result is Resource.Success) {
                savedStateHandle[IS_FETCHED_KEY] = true
            }
        }
    }
}
