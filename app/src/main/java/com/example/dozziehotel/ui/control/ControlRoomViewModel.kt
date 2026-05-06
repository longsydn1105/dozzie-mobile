// File: app/src/main/java/com/example/dozziehotel/ui/control/ControlRoomViewModel.kt

package com.example.dozziehotel.ui.control

import android.util.Log
import androidx.lifecycle.*
import com.example.dozziehotel.data.model.Booking
import com.example.dozziehotel.data.remote.IotCommandRequest
import com.example.dozziehotel.data.remote.IotCommandResponse
import com.example.dozziehotel.data.remote.IotPayload
import com.example.dozziehotel.data.repository.BookingRepository
import com.example.dozziehotel.data.repository.RoomRepository
import com.example.dozziehotel.data.repository.SosRepository
import com.example.dozziehotel.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ControlRoomViewModel(
    private val repository: RoomRepository,
    private val sosRepository: SosRepository,
    private val savedStateHandle: SavedStateHandle,
    private val bookingRepository: BookingRepository
) : ViewModel() {

    val roomId: String = savedStateHandle["room_id"] ?: ""
    val digitalKey: String = savedStateHandle["digital_key"] ?: ""

    private val _iotResult = MutableStateFlow<Resource<IotCommandResponse>>(Resource.Idle())
    val iotResult: StateFlow<Resource<IotCommandResponse>> = _iotResult

    private val _sosResult = MutableStateFlow<Resource<Unit>>(Resource.Idle())
    val sosResult: StateFlow<Resource<Unit>> = _sosResult

    private val _checkoutResult = MutableStateFlow<Resource<Booking>>(Resource.Idle())
    val checkoutResult: StateFlow<Resource<Booking>> = _checkoutResult.asStateFlow()

    fun checkout(bookingId: String) {
        viewModelScope.launch {
            _checkoutResult.value = Resource.Loading()
            val result = bookingRepository.checkout(bookingId)
            _checkoutResult.value = result
        }
    }

    // Reset lại trạng thái để nếu user quay lại màn hình này không bị dính thông báo cũ
    fun resetCheckoutState() {
        _checkoutResult.value = Resource.Idle()
    }

    fun sendCommand(command: String) {
        val topic = "dozzie/capsule/$roomId"
        val request = IotCommandRequest(
            roomId = roomId,
            digitalKey = digitalKey,
            topic = topic,
            payload = IotPayload(command)
        )

        // LOG CHI TIẾT REQUEST ĐỂ KIỂM TRA VỚI DOCUMENT 5.5.1
        Log.d("IOT_DEBUG", "--- START SENDING COMMAND ---")
        Log.d("IOT_DEBUG", "URL: rooms/iot-command")
        Log.d("IOT_DEBUG", "Body Request: roomId=$roomId, digitalKey=$digitalKey, topic=$topic, command=$command")

        viewModelScope.launch {
            _iotResult.value = Resource.Loading()
            val result = repository.sendIotCommand(request)
            _iotResult.value = result

            when (result) {
                is Resource.Success -> {
                    Log.d("IOT_DEBUG", "RECEIVE SUCCESS: ${result.data?.message}")
                }
                is Resource.Error -> {
                    // LOG CHI TIẾT LỖI
                    Log.e("IOT_DEBUG", "RECEIVE ERROR: Status Code (if any) or Message: ${result.message}")
                    Log.e("IOT_DEBUG", "Gợi ý: Kiểm tra digitalKey '$digitalKey' có khớp với đơn hàng Active không.")
                }
                else -> {}
            }
            Log.d("IOT_DEBUG", "--- END PROCESS ---")
        }
    }

    fun sendSosRequest() {
        viewModelScope.launch {
            _sosResult.value = Resource.Loading()
            val result = sosRepository.sendSosAlert(roomId, "Yêu cầu cứu hộ khẩn cấp từ phòng $roomId")
            _sosResult.value = result
        }
    }

    fun handleSosClick() {
        sendCommand("SOS")
        sendSosRequest()
    }
}