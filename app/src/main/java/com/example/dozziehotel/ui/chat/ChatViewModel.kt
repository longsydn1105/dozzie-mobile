package com.example.dozziehotel.ui.chat

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dozziehotel.BuildConfig
import com.example.dozziehotel.data.remote.ChatMessageDto
import com.example.dozziehotel.data.repository.ChatRepository
import com.example.dozziehotel.utils.Resource
import com.google.gson.Gson
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

class ChatViewModel(
    private val repository: ChatRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val TAG = "CHAT_SOCKET"
    private val SOCKET_URL = BuildConfig.BASE_URL
        .removeSuffix("/")
        .removeSuffix("/api")
        .removeSuffix("api")
    private var mSocket: Socket? = null

    private val _messages = MutableStateFlow<List<ChatMessageDto>>(emptyList())
    val messages: StateFlow<List<ChatMessageDto>> = _messages.asStateFlow()

    private val _historyStatus = MutableStateFlow<Resource<Unit>>(Resource.Idle())
    val historyStatus = _historyStatus.asStateFlow()

    private val bookingId: String? get() = savedStateHandle.get<String>("bookingId")
    private val roomId: String? get() = savedStateHandle.get<String>("roomId")

    init {
        setupSocket()
    }

    /**
     * Khởi tạo và cấu hình Socket.io để kết nối với server chat.
     */
    private fun setupSocket() {
        try {
            Log.d(TAG, "Connecting to Socket at: $SOCKET_URL")
            mSocket = IO.socket(SOCKET_URL)

            mSocket?.on(Socket.EVENT_CONNECT) {
                Log.d(TAG, "Socket Connected!")
                joinChat()
            }

            mSocket?.on("receive_message") { args ->
                val data = args[0] as JSONObject
                val message = Gson().fromJson(data.toString(), ChatMessageDto::class.java)

                val currentList = _messages.value.toMutableList()
                if (currentList.none { it.id == message.id }) {
                    currentList.add(message)
                    _messages.value = currentList
                }
            }

            mSocket?.on(Socket.EVENT_DISCONNECT) { Log.d(TAG, "Socket Disconnected") }

            mSocket?.connect()
        } catch (e: Exception) {
            Log.e(TAG, "Socket init error: ${e.message}")
        }
    }

    /**
     * Gửi sự kiện yêu cầu tham gia vào phòng chat dựa trên bookingId.
     */
    private fun joinChat() {
        val bId = bookingId ?: return
        val json = JSONObject().apply {
            put("bookingId", bId)
            put("role", "customer")
        }
        mSocket?.emit("join_chat", json)
    }

    /**
     * Tải lịch sử tin nhắn từ server thông qua repository.
     * Input: [id] mã đơn đặt phòng.
     */
    fun fetchChatHistory(id: String) {
        viewModelScope.launch {
            _historyStatus.value = Resource.Loading()
            val result = repository.getChatHistory(id)
            if (result is Resource.Success) {
                _messages.value = result.data ?: emptyList()
                _historyStatus.value = Resource.Success(Unit)
            } else {
                _historyStatus.value = Resource.Error(result.message ?: "Lỗi tải lịch sử")
            }
        }
    }

    /**
     * Gửi tin nhắn mới tới server thông qua Socket.
     * Input: [text] nội dung tin nhắn.
     */
    fun sendMessage(text: String) {
        if (text.isBlank()) return
        val bId = bookingId ?: return
        val rId = roomId ?: "Unknown"

        val json = JSONObject().apply {
            put("bookingId", bId)
            put("roomId", rId)
            put("senderRole", "customer")
            put("text", text)
        }

        mSocket?.emit("send_message", json)
    }

    override fun onCleared() {
        super.onCleared()
        mSocket?.disconnect()
        mSocket?.off("receive_message")
    }
}
