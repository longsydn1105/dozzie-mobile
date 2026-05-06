package com.example.dozziehotel.data.repository

import com.example.dozziehotel.data.remote.ServicePackageApi
import com.example.dozziehotel.data.remote.ServicePackageDto
import com.example.dozziehotel.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ServicePackageRepository(
    private val serviceApi: ServicePackageApi
) {
    /**
     * Lấy danh sách các gói dịch vụ đang hoạt động (active).
     * Output: Danh sách các gói dịch vụ hoặc thông báo lỗi.
     */
    suspend fun getPackages(): Resource<List<ServicePackageDto>> = withContext(Dispatchers.IO) {
        return@withContext try {
            val response = serviceApi.getActivePackages()
            val body = response.body()

            if (response.isSuccessful && body != null) {
                Resource.Success(body.data.filter { it.isActive })
            } else {
                Resource.Error("Không thể lấy gói dịch vụ")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Lỗi kết nối")
        }
    }
}
