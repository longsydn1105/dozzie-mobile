package com.example.dozziehotel.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.dozziehotel.R
import com.example.dozziehotel.data.model.Room
import com.example.dozziehotel.databinding.ItemRoomBinding

class RoomAdapter : ListAdapter<Room, RoomAdapter.RoomViewHolder>(RoomDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val binding = ItemRoomBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RoomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RoomViewHolder(private val binding: ItemRoomBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(room: Room) {
            binding.apply {
                tvRoomLabel.text = room.label ?: "Kén chưa rõ"
                tvRoomFloor.text = "Tầng: ${room.floor ?: "?"}"
                tvRoomGender.text = "Giới tính: ${room.gender ?: "?"}"
                tvRoomStatus.text = room.status ?: "unknown"

                val context = binding.root.context
                val statusColor = when (room.status?.lowercase()) {
                    "available" -> ContextCompat.getColor(context, R.color.status_success)
                    "occupied" -> ContextCompat.getColor(context, R.color.status_danger)
                    "maintenance" -> ContextCompat.getColor(context, R.color.status_danger)
                    "cleaning" -> ContextCompat.getColor(context, R.color.status_danger) // Can be another color if needed
                    else -> ContextCompat.getColor(context, R.color.dozzie_navy)
                }
                tvRoomStatus.setTextColor(statusColor)
            }
        }
    }

    class RoomDiffCallback : DiffUtil.ItemCallback<Room>() {
        override fun areItemsTheSame(oldItem: Room, newItem: Room): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Room, newItem: Room): Boolean {
            return oldItem == newItem
        }
    }
}
