package com.example.dozziehotel.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.PUT

interface UserApi {

    /**
     * Cập nhật thông tin hồ sơ người dùng.
     * Input: [request] chứa các thông tin cần thay đổi.
     */
    @PUT("users/profile")
    suspend fun updateProfile(
        @Body request: UpdateProfileRequest
    ): Response<UpdateProfileResponse>
}
