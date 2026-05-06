package com.example.dozziehotel.data.repository

import com.example.dozziehotel.data.remote.UpdateProfileRequest
import com.example.dozziehotel.data.remote.UserApi

class UserRepository(
    private val userApi: UserApi
) {
    /**
     * Cập nhật thông tin hồ sơ người dùng.
     * Input: [request] chứa thông tin cần thay đổi.
     */
    suspend fun updateProfile(request: UpdateProfileRequest) = userApi.updateProfile(request)
}
