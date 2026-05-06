package com.example.dozziehotel.data.repository

import android.util.Log
import com.example.dozziehotel.data.model.Booking
import com.example.dozziehotel.data.model.ServicePackage
import com.example.dozziehotel.data.remote.BookingApi
import com.example.dozziehotel.data.remote.BookingNestedDto
import com.example.dozziehotel.data.remote.BookingResponseData
import com.example.dozziehotel.data.remote.CreateBookingRequest
import com.example.dozziehotel.data.remote.MyStatusData
import com.example.dozziehotel.data.remote.ServicePackageApi
import com.example.dozziehotel.data.remote.ServicePackageDto
import com.example.dozziehotel.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.collections.filter

class BookingRepository(
    private val bookingApi: BookingApi,
    private val servicePackageApi: ServicePackageApi
) {
    /**
     * Tạo thông tin xem trước cho đơn đặt phòng.
     * Input: [startTimeIso] thời gian bắt đầu, [selectedPackage] gói dịch vụ đã chọn.
     * Output: Đối tượng [BookingPreview] chứa thông tin tính toán sơ bộ.
     */
    fun buildBookingPreview(startTimeIso: String, selectedPackage: ServicePackage): Resource<BookingPreview> {
        val packageHours = selectedPackage.hours
        val packagePrice = selectedPackage.price
        if (packageHours == null || packagePrice == null) {
            return Resource.Error("Goi dich vu khong hop le")
        }

        val endTimeIso = calculateEndTimeIso(startTimeIso, packageHours)
            ?: return Resource.Error("Sai dinh dang thoi gian bat dau")

        return Resource.Success(
            BookingPreview(
                packageId = selectedPackage.id.orEmpty(),
                packageName = selectedPackage.name.orEmpty(),
                startTimeIso = startTimeIso,
                endTimeIso = endTimeIso,
                totalPrice = packagePrice
            )
        )
    }

    /**
     * Gửi yêu cầu đặt phòng lên hệ thống.
     * Input: [roomId] ID phòng, [preview] thông tin xem trước đã xác nhận.
     * Output: Dữ liệu đơn đặt phòng mới tạo hoặc lỗi.
     */
    suspend fun createBooking(roomId: String, preview: BookingPreview): Resource<BookingResponseData> {
        return try {
            val request = CreateBookingRequest(
                roomId = roomId,
                packageId = preview.packageId,
                startTime = preview.startTimeIso
            )

            Log.d("CREATE_BOOKING", "Request: $request")

            val response = bookingApi.createBooking(request)

            Log.d("CREATE_BOOKING", "Code: ${response.code()} | Body: ${response.body()}")

            if (response.isSuccessful) {
                val body = response.body()

                if (body?.success == true && body.data != null) {
                    Log.d("CREATE_BOOKING", "Thành công: ${body.message}")
                    Resource.Success(body.data)
                } else {
                    val msg = body?.message ?: "Đặt phòng thất bại (Server từ chối)"
                    Log.e("CREATE_BOOKING", "Lỗi Business: $msg")
                    Resource.Error(msg)
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("CREATE_BOOKING", "Lỗi HTTP: $errorBody")
                Resource.Error("Lỗi hệ thống (${response.code()})")
            }
        } catch (e: Exception) {
            Log.e("CREATE_BOOKING", "Exception: ${e.message}")
            Resource.Error("Lỗi kết nối: ${e.localizedMessage}")
        }
    }

    /**
     * Lấy danh sách lịch sử đặt phòng của người dùng hiện tại.
     * Output: Danh sách các đơn đặt phòng.
     */
    suspend fun getMyBookings(): Resource<List<BookingNestedDto>> {
        return try {
            val response = bookingApi.getMyBookings()
            Log.d("MY_BOOKING", "Raw Response: ${response.body()}")

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true) {
                    Resource.Success(body.data ?: emptyList())
                } else {
                    Resource.Error(body?.message ?: "Không thể lấy danh sách đơn đặt")
                }
            } else {
                Resource.Error("Lỗi kết nối: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("MY_BOOKING", "Exception: ${e.message}")
            Resource.Error("Lỗi hệ thống: ${e.localizedMessage}")
        }
    }

    /**
     * Lấy trạng thái đặt phòng hiện tại (phòng đang ở, active booking).
     * Output: Thông tin trạng thái đặt phòng hoặc lỗi.
     */
    suspend fun getMyStatus(): Resource<MyStatusData> {
        return try {
            val response = bookingApi.getMyStatus()

            Log.d("MY_STATUS", "HTTP Code: ${response.code()}")
            Log.d("MY_STATUS", "Raw Body: ${response.body()}")

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    Log.d("MY_STATUS", "Parsed Data: ${body.data}")
                    Resource.Success(body.data)
                } else {
                    Log.e("MY_STATUS", "Business Error: ${body?.message}")
                    Resource.Error(body?.message ?: "Lỗi lấy trạng thái")
                }
            } else {
                val errorMsg = response.errorBody()?.string()
                Log.e("MY_STATUS", "Server Error Body: $errorMsg")
                Resource.Error("Lỗi kết nối: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("MY_STATUS", "Exception: ${e.message}")
            Resource.Error("Hệ thống: ${e.localizedMessage}")
        }
    }

    /**
     * Lấy thông tin chi tiết của một đơn đặt phòng.
     * Input: [bookingId] mã đơn đặt phòng.
     */
    suspend fun getBookingDetail(bookingId: String) = bookingApi.getBookingDetail(bookingId)

    /**
     * Hủy bỏ một đơn đặt phòng.
     * Input: [bookingId] mã đơn đặt phòng cần hủy.
     */
    suspend fun cancelBooking(bookingId: String) = bookingApi.cancelBooking(bookingId)

    /**
     * Kiểm tra tình trạng trống của phòng.
     * Input: [status] trạng thái cần lọc.
     */
    suspend fun checkAvailability(status: String) = bookingApi.checkAvailability(status)

    /**
     * Tính toán thời gian kết thúc dựa trên thời gian bắt đầu và số giờ của gói.
     * Input: [startTimeIso] ISO string, [packageHours] số giờ.
     * Output: ISO string thời gian kết thúc hoặc null nếu lỗi format.
     */
    private fun calculateEndTimeIso(startTimeIso: String, packageHours: Int): String? {
        return runCatching {
            val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val startDate = formatter.parse(startTimeIso) ?: return null
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                time = startDate
                add(Calendar.HOUR_OF_DAY, packageHours)
            }
            formatter.format(calendar.time)
        }.getOrNull()
    }


    suspend fun checkout(bookingId: String): Resource<Booking> {
        return try {
            val response = bookingApi.checkout(bookingId)
            val body = response.body()

            if (response.isSuccessful && body != null && body.success == true) {
                Resource.Success(body.data!!)
            } else {
                Resource.Error(body?.message ?: "Checkout thất bại")
            }
        } catch (e: Exception) {
            Log.e("CHECKOUT_ERR", e.message ?: "Unknown error")
            Resource.Error("Lỗi kết nối: ${e.localizedMessage}")
        }
    }
}

data class BookingPreview(
    val packageId: String,
    val packageName: String,
    val startTimeIso: String,
    val endTimeIso: String,
    val totalPrice: Double
)
