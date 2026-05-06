package com.example.dozziehotel.ui.booking

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.dozziehotel.R
import com.example.dozziehotel.data.remote.BookingNestedDto
import com.example.dozziehotel.databinding.ItemMyBookingBinding
import java.text.SimpleDateFormat
import java.util.*

class MyBookingAdapter : RecyclerView.Adapter<MyBookingAdapter.BookingViewHolder>() {

    private var bookings = listOf<BookingNestedDto>()

    fun setData(newList: List<BookingNestedDto>) {
        bookings = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val binding = ItemMyBookingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val item = bookings[position]
        with(holder.binding) {
            // Đổ dữ liệu text
            tvRoomLabel.text = item.room?.label ?: "N/A"
            tvPackageName.text = item.servicePackage?.name ?: "N/A"
            tvKey.text = "CHÌA KHÓA PHÒNG: ${item.digitalKey ?: "------"}"
            tvTotalPrice.text = String.format("%,.0f đ", item.totalPrice ?: 0.0)

            // Xử lý Ngày/Giờ (ISO String -> HH:mm & dd/MM/yyyy)
            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val timeOutFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val dateOutFormat = SimpleDateFormat("dd/M/yyyy", Locale.getDefault())

            item.startTime?.let {
                val date = isoFormat.parse(it)
                tvStartTime.text = timeOutFormat.format(date!!)
                tvStartDate.text = dateOutFormat.format(date)
            }
            item.endTime?.let {
                val date = isoFormat.parse(it)
                tvEndTime.text = timeOutFormat.format(date!!)
                tvEndDate.text = dateOutFormat.format(date)
            }

            // Xử lý Trạng thái & Màu sắc (Dựa trên colors.xml)
            when (item.status) {
                "cancelled", "admin_cancelled" -> {
                    tvStatus.text = "✘ ĐÃ HỦY"
                    tvStatus.setTextColor(ContextCompat.getColor(root.context, R.color.dozzie_danger))
                    tvStatus.setBackgroundResource(R.drawable.bg_tag_danger)
                }
                "active" -> {
                    tvStatus.text = "✔ ĐANG Ở"
                    tvStatus.setTextColor(ContextCompat.getColor(root.context, R.color.dozzie_success))
                    tvStatus.setBackgroundResource(R.drawable.bg_tag_success)
                }
                "completed" -> {
                    tvStatus.text = "DONE"
                    tvStatus.setTextColor(ContextCompat.getColor(root.context, R.color.dozzie_blue))
                    tvStatus.setBackgroundColor(android.graphics.Color.parseColor("#E0F2FE")) // Xanh dương nhạt
                }
                else -> {
                    tvStatus.text = item.status?.uppercase()
                    tvStatus.setTextColor(ContextCompat.getColor(root.context, R.color.dozzie_navy))
                    tvStatus.setBackgroundColor(android.graphics.Color.parseColor("#F3F4F6"))
                }
            }
        }
    }

    override fun getItemCount() = bookings.size

    class BookingViewHolder(val binding: ItemMyBookingBinding) : RecyclerView.ViewHolder(binding.root)
}
