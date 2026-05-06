package com.example.dozziehotel.data.remote

import com.example.dozziehotel.data.model.Booking
import com.example.dozziehotel.data.model.Invoice
import com.example.dozziehotel.data.model.Room
import com.example.dozziehotel.data.model.ServicePackage
import com.example.dozziehotel.data.model.User
import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    @SerializedName("fullName")
    val fullName: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String
)

data class LoginRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String,
    @SerializedName("fcmToken")
    val fcmToken: String? = null
)

data class UpdateFcmTokenRequest(
    @SerializedName("fcmToken")
    val fcmToken: String
)

data class UpdateProfileRequest(
    @SerializedName("fullName")
    val fullName: String? = null,
    @SerializedName("phone")
    val phone: String? = null,
    @SerializedName("password")
    val password: String? = null
)

data class CreateBookingRequest(
    @SerializedName("roomId")
    val roomId: String,
    @SerializedName("packageId")
    val packageId: String,
    @SerializedName("startTime")
    val startTime: String
)

data class SimpleAuthUser(
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("fullName")
    val fullName: String? = null,
    @SerializedName("email")
    val email: String? = null,
    @SerializedName("role")
    val role: String? = null,
    @SerializedName("phone")
    val phone: String? = null
)

data class LoginResponse(
    @SerializedName("success")
    val success: Boolean? = null,
    @SerializedName("token")
    val token: String? = null,
    @SerializedName("user")
    val user: SimpleAuthUser? = null,
    @SerializedName("message")
    val message: String? = null
)

data class BaseResponse<T>(
    @SerializedName("success")
    val success: Boolean? = null,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("data")
    val data: T? = null
)

data class ListResponse<T>(
    @SerializedName("success")
    val success: Boolean? = null,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("count")
    val count: Int? = null,
    @SerializedName("data")
    val data: List<T>? = null
)

data class RegisterData(
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("fullName")
    val fullName: String? = null,
    @SerializedName("email")
    val email: String? = null,
    @SerializedName("role")
    val role: String? = null
)

data class BookingAvailability(
    @SerializedName("_id")
    val id: String? = null,
    @SerializedName("roomId")
    val roomId: String? = null,
    @SerializedName("startTime")
    val startTime: String? = null,
    @SerializedName("endTime")
    val endTime: String? = null,
    @SerializedName("status")
    val status: String? = null
)

data class ServicePackageDto(
    @SerializedName("_id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("hours")
    val hours: Int,

    @SerializedName("price")
    val price: Double,

    @SerializedName("isActive")
    val isActive: Boolean
) {
    fun getDisplayText(): String {
        return "$name - ${hours}h - ${price.toInt()} VND"
    }
}

data class BookingResponseData(
    @SerializedName("booking")
    val booking: Booking,
    @SerializedName("invoice")
    val invoice: Any? = null // Có thể để Any nếu chưa dùng tới Invoice
)
data class ServicePackageResponse(
    val success: Boolean,
    val data: List<ServicePackageDto>
)

data class InvoiceDto(
    @SerializedName("_id") val id: String?,
    @SerializedName("invoiceCode") val invoiceCode: String?,
    @SerializedName("totalAmount") val totalAmount: Double?,
    @SerializedName("paymentStatus") val paymentStatus: String?,
    @SerializedName("bookingId") val booking: BookingNestedDto?, // Object lồng nhau
    @SerializedName("createdAt") val createdAt: String?
)

data class BookingNestedDto(
    @SerializedName("_id") val id: String?,
    @SerializedName("roomId") val room: RoomNestedDto?,     // nhưng theo doc 5.4.1 là String "M-01"
    @SerializedName("packageId") val servicePackage: PackageNestedDto?,
    @SerializedName("startTime") val startTime: String?,
    @SerializedName("endTime") val endTime: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("totalPrice") val totalPrice: Double?,
    @SerializedName("digitalKey") val digitalKey: String?
)
data class RoomNestedDto(
    @SerializedName("_id") val id: String?,
    @SerializedName("label") val label: String?
)

data class PackageNestedDto(
    @SerializedName("_id") val id: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("hours") val hours: Int?
)

data class ActiveBooking(
    @SerializedName("_id") val id: String?,
    @SerializedName("roomId") val roomId: String?,
    @SerializedName("packageId") val packageId: String?,
    @SerializedName("startTime") val startTime: String?,
    @SerializedName("endTime") val endTime: String?,
    @SerializedName("digitalKey") val digitalKey: String?,
    @SerializedName("status") val status: String?
)

data class MyStatusData(
    @SerializedName("canBookNew") val canBookNew: Boolean,
    @SerializedName("pendingCount") val pendingCount: Int,
    @SerializedName("activeBooking") val activeBooking: ActiveBooking?
)

data class IotPayload(
    @SerializedName("command") val command: String
)

data class IotCommandRequest(
    @SerializedName("roomId") val roomId: String,
    @SerializedName("digitalKey") val digitalKey: String,
    @SerializedName("topic") val topic: String,
    @SerializedName("payload") val payload: IotPayload
)

data class IotCommandResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String
)

data class ChatMessageDto(
    @SerializedName("_id") val id: String,
    @SerializedName("bookingId") val bookingId: String,
    @SerializedName("roomId") val roomId: String,
    @SerializedName("senderRole") val senderRole: String,
    @SerializedName("text") val text: String,
    @SerializedName("isRead") val isRead: Boolean,
    @SerializedName("createdAt") val createdAt: String
)

data class CreateSosRequest(
    @SerializedName("roomId")
    val roomId: String,
    @SerializedName("message")
    val message: String
)

typealias CheckoutResponse = BaseResponse<Booking>
typealias SosResponse = BaseResponse<Unit>
typealias ChatHistoryResponse = BaseResponse<List<ChatMessageDto>>
typealias RegisterResponse = BaseResponse<RegisterData>
typealias UpdateProfileResponse = BaseResponse<User>
typealias CreateBookingResponse = BaseResponse<BookingResponseData>
typealias BookingDetailResponse = BaseResponse<Booking>
typealias CancelBookingResponse = BaseResponse<Booking>
typealias PayInvoiceResponse = BaseResponse<Invoice>
typealias MyBookingsResponse = ListResponse<BookingNestedDto>
typealias AvailabilityResponse = ListResponse<BookingAvailability>
typealias RoomsResponse = ListResponse<Room>
typealias MyInvoicesResponse = ListResponse<InvoiceDto>
typealias MyStatusResponse = BaseResponse<MyStatusData>