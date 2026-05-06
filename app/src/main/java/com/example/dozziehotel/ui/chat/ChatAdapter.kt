package com.example.dozziehotel.ui.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dozziehotel.data.remote.ChatMessageDto
import com.example.dozziehotel.databinding.ItemChatLeftBinding
import com.example.dozziehotel.databinding.ItemChatRightBinding
import java.text.SimpleDateFormat
import java.util.*

class ChatAdapter(private val currentUserName: String) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var messages = listOf<ChatMessageDto>()

    /**
     * Cập nhật danh sách tin nhắn và làm mới giao diện.
     * Input: [list] danh sách tin nhắn mới.
     */
    fun submitList(list: List<ChatMessageDto>) {
        messages = list
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].senderRole == "customer") 1 else 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 1) {
            val binding = ItemChatRightBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            RightViewHolder(binding)
        } else {
            val binding = ItemChatLeftBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            LeftViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = messages[position]
        val time = formatTime(msg.createdAt)
        if (holder is RightViewHolder) holder.bind(msg, currentUserName, time)
        else if (holder is LeftViewHolder) holder.bind(msg, time)
    }

    override fun getItemCount() = messages.size

    inner class RightViewHolder(val binding: ItemChatRightBinding) : RecyclerView.ViewHolder(binding.root) {
        /**
         * Hiển thị nội dung tin nhắn của người dùng hiện tại (bên phải).
         */
        fun bind(msg: ChatMessageDto, name: String, time: String) {
            binding.tvMessage.text = msg.text
            binding.tvName.text = name
            binding.tvTime.text = time
        }
    }

    inner class LeftViewHolder(val binding: ItemChatLeftBinding) : RecyclerView.ViewHolder(binding.root) {
        /**
         * Hiển thị nội dung tin nhắn từ phía quản trị viên/lễ tân (bên trái).
         */
        fun bind(msg: ChatMessageDto, time: String) {
            binding.tvMessage.text = msg.text
            binding.tvName.text = "Lễ tân (Admin)"
            binding.tvTime.text = time
        }
    }

    /**
     * Chuyển đổi chuỗi thời gian ISO sang định dạng giờ:phút (HH:mm).
     * Input: [isoString] chuỗi thời gian từ server.
     * Output: Chuỗi thời gian đã định dạng.
     */
    private fun formatTime(isoString: String): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            val date = sdf.parse(isoString)
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(date!!)
        } catch (e: Exception) { "" }
    }
}
