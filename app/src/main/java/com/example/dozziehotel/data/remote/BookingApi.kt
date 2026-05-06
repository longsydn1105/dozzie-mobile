package com.example.dozziehotel.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface BookingApi {

    /**
     * Gửi yêu cầu đặt phòng mới.
     * Input: [request] thông tin phòng, gói dịch vụ và thời gian bắt đầu.
     */
    @POST("bookings")
    suspend fun createBooking(
        @Body request: CreateBookingRequest
    ): Response<CreateBookingResponse>

    /**
     * Lấy danh sách lịch sử các đơn đặt phòng của tôi.
     */
    @GET("bookings/my-bookings")
    suspend fun getMyBookings(): Response<MyBookingsResponse>

    /**
     * Lấy thông tin chi tiết một đơn đặt phòng cụ thể.
     * Input: [bookingId] ID của đơn đặt phòng.
     */
    @GET("bookings/{id}")
    suspend fun getBookingDetail(
        @Path("id") bookingId: String
    ): Response<BookingDetailResponse>

    /**
     * Yêu cầu hủy một đơn đặt phòng đang chờ hoặc đã xác nhận.
     * Input: [bookingId] ID của đơn đặt phòng cần hủy.
     */
    @PATCH("bookings/{id}/cancel")
    suspend fun cancelBooking(
        @Path("id") bookingId: String
    ): Response<CancelBookingResponse>

    /**
     * Kiểm tra tình trạng phòng trống dựa trên trạng thái.
     * Input: [status] trạng thái cần lọc (ví dụ: available).
     */
    @GET("bookings")
    suspend fun checkAvailability(
        @Query("status") status: String
    ): Response<AvailabilityResponse>

    /**
     * Lấy thông tin đặt phòng hiện tại (phòng đang ở).
     */
    @GET("bookings/my-status")
    suspend fun getMyStatus(): Response<MyStatusResponse>

    @PATCH("bookings/{id}/checkout")
    suspend fun checkout(
        @Path("id") bookingId: String
    ): Response<CheckoutResponse>
}
