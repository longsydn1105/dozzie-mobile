package com.example.dozziehotel.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface RoomApi {

    /**
     * Lấy danh sách tất cả các phòng hiện có.
     */
    @GET("rooms")
    suspend fun getAllRooms(): Response<RoomsResponse>

    /**
     * Gửi lệnh điều khiển thiết bị IoT trong phòng.
     * Input: [request] chứa thông tin lệnh (device, action, state).
     */
    @POST("rooms/iot-command")
    suspend fun sendIotCommand(@Body request: IotCommandRequest): Response<IotCommandResponse>
}
