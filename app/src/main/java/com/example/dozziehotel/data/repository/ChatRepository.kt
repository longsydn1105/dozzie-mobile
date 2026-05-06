package com.example.dozziehotel.data.repository

import android.util.Log
import com.example.dozziehotel.data.remote.ChatApi
import com.example.dozziehotel.data.remote.ChatMessageDto
import com.example.dozziehotel.utils.Resource

class ChatRepository(
    private val chatApi: ChatApi
) {
    /**
     * Lấy lịch sử trò chuyện theo mã đặt phòng.
     * Input: [bookingId] mã đặt phòng.
     * Output: Danh sách tin nhắn hoặc lỗi.
     */
    suspend fun getChatHistory(bookingId: String): Resource<List<ChatMessageDto>> {
        return try {
            val response = chatApi.getChatHistory(bookingId)
            if (response.isSuccessful) {
                val data = response.body()?.data ?: emptyList()
                Resource.Success(data)
            } else {
                Resource.Error("Lỗi lấy lịch sử chat: ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e("REPO_ERROR", "Chat History Error: ${e.localizedMessage}")
            Resource.Error("Lỗi hệ thống: ${e.javaClass.simpleName}")
        }
    }
}
