package com.example.dozziehotel.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dozziehotel.data.model.IntroItem
import com.example.dozziehotel.databinding.ItemIntroBinding

class IntroAdapter(private val items: List<IntroItem>, private val onExploreClick: () -> Unit) :
    RecyclerView.Adapter<IntroAdapter.IntroViewHolder>() {

    inner class IntroViewHolder(val binding: ItemIntroBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IntroViewHolder {
        val binding = ItemIntroBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return IntroViewHolder(binding)
    }

    override fun onBindViewHolder(holder: IntroViewHolder, position: Int) {
        val item = items[position]
        holder.binding.apply {
            tvMainTitle.text = item.title
            tvDesc.text = item.description
            ivIntro.setImageResource(item.imageRes)
            btnExplore.setOnClickListener { onExploreClick() }
        }
    }

    override fun getItemCount() = items.size
}