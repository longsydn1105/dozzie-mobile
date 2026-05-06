package com.example.dozziehotel.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface SosApi {
    /**
     * Gửi yêu cầu cứu hộ khẩn cấp.
     * Input: [request] chứa mã phòng và nội dung thông báo.
     */
    @POST("sos/emergency")
    suspend fun sendSosAlert(
        @Body request: CreateSosRequest
    ): Response<SosResponse>
}
