package com.example.dozziehotel.ui.auth

import androidx.lifecycle.ViewModel
import com.example.dozziehotel.data.repository.AuthRepository
import com.example.dozziehotel.data.repository.UserRepository

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    fun authRepo() = authRepository
    fun userRepo() = userRepository
}
