package com.example.dozziehotel.data.repository

import android.util.Log
import com.example.dozziehotel.data.remote.AuthApi
import com.example.dozziehotel.data.remote.LoginRequest
import com.example.dozziehotel.data.remote.LoginResponse
import com.example.dozziehotel.data.remote.RegisterResponse
import com.example.dozziehotel.data.remote.RegisterRequest
import com.example.dozziehotel.utils.Resource
import com.example.dozziehotel.data.local.PreferenceManager
import com.example.dozziehotel.data.remote.UpdateFcmTokenRequest
import kotlin.text.clear

class AuthRepository(
    private val authApi: AuthApi,
    private val pref: PreferenceManager
) {

    /**
     * Thực hiện đăng ký tài khoản người dùng mới.
     * Input: [request] thông tin đăng ký.
     * Output: Kết quả thành công hoặc thông báo lỗi.
     */
    suspend fun register(request: RegisterRequest): Resource<RegisterResponse> {
        return try {
            val response = authApi.register(request)
            val body = response.body()
            if (response.isSuccessful && body != null && body.success == true) {
                Resource.Success(body)
            } else {
                Resource.Error(body?.message ?: "Đăng ký thất bại")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Không thể kết nối máy chủ")
        }
    }

    /**
     * Thực hiện đăng nhập và lưu token, thông tin người dùng nếu thành công.
     * Input: [request] thông tin đăng nhập.
     * Output: Dữ liệu đăng nhập hoặc lỗi.
     */
    suspend fun login(request: LoginRequest): Resource<LoginResponse> {
        return try {
            Log.d("AUTH_DEBUG", "Login Request Body: $request")
            val response = authApi.login(request)
            val body = response.body()
            Log.d("AUTH_DEBUG", "Login Response Body: $body")
            if (response.isSuccessful && body != null && body.success == true && !body.token.isNullOrBlank()) {
                body.token.let { pref.saveToken(it) }
                body.user?.let { pref.saveUser(it) }
                Resource.Success(body)
            } else {
                Resource.Error(body?.message ?: "Đăng nhập thất bại")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Không thể kết nối máy chủ")
        }
    }

    suspend fun updateFcmToken(token: String): Resource<Unit> {
        return try {
            val response = authApi.updateFcmToken(UpdateFcmTokenRequest(token))
            if (response.isSuccessful) Resource.Success(Unit)
            else Resource.Error("Cập nhật token thất bại")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Lỗi kết nối")
        }
    }

    suspend fun logout(fcmToken: String?): Resource<Unit> {
        return try {
            if (!fcmToken.isNullOrBlank()) {
                authApi.logout(UpdateFcmTokenRequest(fcmToken))
            }
            pref.clearData()
            Resource.Success(Unit)
        } catch (e: Exception) {
            pref.clearData()
            Resource.Error(e.message ?: "Lỗi khi đăng xuất")
        }
    }

}
