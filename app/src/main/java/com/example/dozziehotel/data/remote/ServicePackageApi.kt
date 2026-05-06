package com.example.dozziehotel.data.remote

import retrofit2.Response
import retrofit2.http.GET

interface ServicePackageApi {
    /**
     * Lấy danh sách các gói dịch vụ đang hoạt động.
     */
    @GET("service-packages")
    suspend fun getActivePackages(): Response<ServicePackageResponse>
}
