package com.example.dozziehotel.ui.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dozziehotel.data.model.Room
import com.example.dozziehotel.data.repository.RoomRepository
import com.example.dozziehotel.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RoomViewModel(private val repository: RoomRepository) : ViewModel() {

    private val _rooms = MutableStateFlow<Resource<List<Room>>>(Resource.Idle())
    val rooms: StateFlow<Resource<List<Room>>> = _rooms.asStateFlow()

    fun fetchAllRooms() {
        viewModelScope.launch {
            _rooms.value = Resource.Loading()
            _rooms.value = repository.getAllRooms()
        }
    }
}