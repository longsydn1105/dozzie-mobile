package com.example.dozziehotel.data.repository

import android.util.Log
import com.example.dozziehotel.data.remote.CreateSosRequest
import com.example.dozziehotel.data.remote.SosApi
import com.example.dozziehotel.utils.Resource

class SosRepository(
    private val sosApi: SosApi
) {
    /**
     * Gửi yêu cầu cứu hộ khẩn cấp (SOS).
     * Input: [roomId] ID phòng, [message] nội dung yêu cầu.
     * Output: Thành công hoặc thông báo lỗi.
     */
    suspend fun sendSosAlert(roomId: String, message: String): Resource<Unit> {
        return try {
            val response = sosApi.sendSosAlert(CreateSosRequest(roomId, message))
            val body = response.body()

            Log.d("REPO_DEBUG", "SOS Server Response: $body")

            if (response.isSuccessful && body?.success == true) {
                Resource.Success(Unit)
            } else {
                val errorMsg = body?.message ?: "Gửi yêu cầu cứu hộ thất bại"
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            Log.e("REPO_ERROR", "Sos Error: ${e.localizedMessage}")
            Resource.Error("Lỗi kết nối hệ thống: ${e.javaClass.simpleName}")
        }
    }
}
