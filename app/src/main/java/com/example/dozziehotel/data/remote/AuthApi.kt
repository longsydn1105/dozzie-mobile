package com.example.dozziehotel.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.PATCH
import retrofit2.http.POST

interface AuthApi {

    /**
     * Gửi yêu cầu đăng ký tài khoản mới.
     * Input: [request] chứa thông tin đăng ký (email, password, name).
     */
    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<RegisterResponse>

    /**
     * Gửi yêu cầu đăng nhập vào hệ thống.
     * Input: [request] chứa thông tin đăng nhập (email, password).
     */
    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>


    @PATCH("auth/update-fcm-token")
    suspend fun updateFcmToken(
        @Body request: UpdateFcmTokenRequest
    ): Response<BaseResponse<Unit>>


    @POST("auth/logout")
    suspend fun logout(
        @Body request: UpdateFcmTokenRequest
    ): Response<BaseResponse<Unit>>
}
