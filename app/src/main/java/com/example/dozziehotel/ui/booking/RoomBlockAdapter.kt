package com.example.dozziehotel.ui.bookingimport

import android.annotation.SuppressLint

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.dozziehotel.data.model.Room
import com.example.dozziehotel.databinding.ItemRoomBlockBinding

class RoomBlockAdapter(private val onRoomSelected: (Room?) -> Unit) :
    RecyclerView.Adapter<RoomBlockAdapter.BlockViewHolder>() {

    // Giữ nguyên logic Pair để hiển thị 2 phòng 1 cột
    private var roomPairs = listOf<Pair<Pair<Room, Boolean>, Pair<Room, Boolean>?>>()
    private var selectedRoomId: String? = null

    @SuppressLint("NotifyDataSetChanged")
    fun submitRooms(rooms: List<Room>) {
        // Cập nhật danh sách và trạng thái chọn
        roomPairs = rooms.sortedBy { it.id }.chunked(2).map {
            val top = it[0]
            val bottom = it.getOrNull(1)
            Pair(
                Pair(top, top.id == selectedRoomId),
                bottom?.let { b -> Pair(b, b.id == selectedRoomId) }
            )
        }
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clearSelection() {
        selectedRoomId = null
        refreshPairs()
        notifyDataSetChanged()
    }

    private fun refreshPairs() {
        roomPairs = roomPairs.map { pair ->
            val top = pair.first.first
            val bottom = pair.second?.first
            Pair(
                Pair(top, top.id == selectedRoomId),
                bottom?.let { b -> Pair(b, b.id == selectedRoomId) }
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlockViewHolder {
        val binding = ItemRoomBlockBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BlockViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BlockViewHolder, position: Int) = holder.bind(roomPairs[position])

    override fun getItemCount(): Int = roomPairs.size

    inner class BlockViewHolder(val binding: ItemRoomBlockBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(pair: Pair<Pair<Room, Boolean>, Pair<Room, Boolean>?>) {
            val (topData, bottomData) = pair
            setupRoomUI(binding.layoutTopRoom, binding.tvTopId, binding.tvTopStatus, topData.first, topData.second)

            if (bottomData != null) {
                binding.layoutBottomRoom.isVisible = true
                setupRoomUI(binding.layoutBottomRoom, binding.tvBottomId, binding.tvBottomStatus, bottomData.first, bottomData.second)
            } else {
                binding.layoutBottomRoom.isVisible = false
            }
        }

        private fun setupRoomUI(layout: View, tvId: TextView, tvStatus: TextView, room: Room, isSelected: Boolean) {
            tvId.text = room.label ?: room.id
            val isAvailable = room.status == "available"

            if (isAvailable) {
                layout.alpha = 1.0f
                tvStatus.text = ""

                // --- LOGIC MÀU DOZZIE BLUE (#219EBC) ---
                if (isSelected) {
                    layout.setBackgroundColor(Color.parseColor("#219EBC"))
                    tvId.setTextColor(Color.WHITE)
                } else {
                    layout.setBackgroundColor(Color.WHITE)
                    tvId.setTextColor(Color.BLACK)
                }

                layout.setOnClickListener {
                    // Nếu bấm vào phòng đã chọn -> Bỏ chọn (null). Nếu bấm phòng khác -> Chọn phòng đó.
                    selectedRoomId = if (selectedRoomId == room.id) null else room.id
                    onRoomSelected(if (selectedRoomId == null) null else room)
                    refreshPairs()
                    notifyDataSetChanged()
                }
            } else {
                layout.setBackgroundColor(Color.parseColor("#F3F4F6"))
                tvId.setTextColor(Color.GRAY)
                layout.alpha = 0.5f
                tvStatus.text = room.status?.uppercase()
                layout.setOnClickListener(null)
            }
        }
    }
}