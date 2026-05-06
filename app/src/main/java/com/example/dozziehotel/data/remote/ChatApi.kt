package com.example.dozziehotel.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ChatApi {
    /**
     * Lấy lịch sử tin nhắn của một cuộc trò chuyện dựa trên mã đặt phòng.
     * Input: [bookingId] mã đơn đặt phòng.
     */
    @GET("chat/history/{bookingId}")
    suspend fun getChatHistory(
        @Path("bookingId") bookingId: String
    ): Response<ChatHistoryResponse>
}
