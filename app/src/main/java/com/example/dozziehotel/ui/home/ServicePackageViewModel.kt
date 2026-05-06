package com.example.dozziehotel.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dozziehotel.data.remote.ServicePackageDto
import com.example.dozziehotel.data.repository.ServicePackageRepository
import com.example.dozziehotel.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ServicePackageViewModel(
    private val repository: ServicePackageRepository
) : ViewModel() {

    private val _packages = MutableStateFlow<Resource<List<ServicePackageDto>>>(Resource.Idle())
    val packages: StateFlow<Resource<List<ServicePackageDto>>> = _packages.asStateFlow()

    fun fetchActivePackages() {
        viewModelScope.launch {
            _packages.value = Resource.Loading()
            // Gọi repository để lấy dữ liệu từ API
            val result = repository.getPackages()
            _packages.value = result
        }
    }
}
