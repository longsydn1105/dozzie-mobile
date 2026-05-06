package com.example.dozziehotel.data.repository

import com.example.dozziehotel.data.model.Room
import com.example.dozziehotel.data.remote.IotCommandRequest
import com.example.dozziehotel.data.remote.IotCommandResponse
import com.example.dozziehotel.data.remote.RoomApi
import com.example.dozziehotel.utils.Resource

class RoomRepository(
    private val roomApi: RoomApi
) {
    /**
     * Lấy danh sách tất cả các phòng hiện có.
     * Output: Danh sách phòng hoặc thông báo lỗi.
     */
    suspend fun getAllRooms(): Resource<List<Room>> {
        return try {
            val response = roomApi.getAllRooms()
            val body = response.body()
            if (response.isSuccessful && body?.data != null) {
                Resource.Success(body.data)
            } else {
                Resource.Error(body?.message ?: "Không thể tải danh sách phòng")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Không thể kết nối máy chủ")
        }
    }

    /**
     * Gửi lệnh điều khiển thiết bị IoT trong phòng.
     * Input: [request] chứa thông tin lệnh (device, action, state).
     * Output: Kết quả thực hiện lệnh từ server.
     */
    suspend fun sendIotCommand(request: IotCommandRequest): Resource<IotCommandResponse> {
        return try {
            val response = roomApi.sendIotCommand(request)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                Resource.Success(body)
            } else {
                Resource.Error(body?.message ?: "Lệnh không hợp lệ hoặc hết hạn")
            }
        } catch (e: Exception) {
            Resource.Error("Lỗi kết nối: ${e.message}")
        }
    }
}
