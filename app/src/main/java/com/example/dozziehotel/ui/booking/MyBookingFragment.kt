package com.example.dozziehotel.ui.booking

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dozziehotel.R
import com.example.dozziehotel.databinding.FragmentMyBookingBinding
import com.example.dozziehotel.utils.Resource
import kotlinx.coroutines.flow.collect
import org.koin.androidx.viewmodel.ext.android.viewModel

class MyBookingFragment : Fragment(R.layout.fragment_my_booking) {
    private var _binding: FragmentMyBookingBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MyBookingViewModel by viewModel()
    private val adapter = MyBookingAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMyBookingBinding.bind(view)

        setupRecyclerView()
        observeData()
        viewModel.fetchMyBookings()
    }

    private fun setupRecyclerView() {
        binding.rvMyBookings.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMyBookings.adapter = adapter
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.bookings.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        // Hiển thị loading nếu có
                    }
                    is Resource.Success -> {
                        adapter.setData(resource.data ?: emptyList())
                    }
                    is Resource.Error -> {
                        Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}