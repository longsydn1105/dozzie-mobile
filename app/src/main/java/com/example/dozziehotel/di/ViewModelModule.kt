package com.example.dozziehotel.di

import com.example.dozziehotel.ui.auth.AuthViewModel
import com.example.dozziehotel.ui.auth.LoginViewModel
import com.example.dozziehotel.ui.auth.RegisterViewModel
import com.example.dozziehotel.ui.booking.BookingViewModel
import com.example.dozziehotel.ui.booking.MyBookingViewModel
import com.example.dozziehotel.ui.booking.RoomViewModel
import com.example.dozziehotel.ui.chat.ChatViewModel
import com.example.dozziehotel.ui.control.ControlRoomViewModel
import com.example.dozziehotel.ui.home.HomeViewModel
import com.example.dozziehotel.ui.home.ServicePackageViewModel
import com.example.dozziehotel.ui.invoice.InvoiceViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { AuthViewModel(get(), get()) }
    viewModel { LoginViewModel(get()) }
    viewModel { RegisterViewModel(get()) }
    viewModel { HomeViewModel(get(), get()) }
    viewModel { BookingViewModel(get()) }
    viewModel { InvoiceViewModel(get()) }
    viewModel { ServicePackageViewModel(get()) }
    viewModel { RoomViewModel(get()) }
    viewModel { MyBookingViewModel(get()) }
    viewModel { ControlRoomViewModel(get(), get(), get(), get()) }
    viewModel { ChatViewModel(get(), get()) }
}
