package com.example.dozziehotel.di

import com.example.dozziehotel.data.local.PreferenceManager
import com.example.dozziehotel.data.repository.AuthRepository
import com.example.dozziehotel.data.repository.BookingRepository
import com.example.dozziehotel.data.repository.ChatRepository
import com.example.dozziehotel.data.repository.InvoiceRepository
import com.example.dozziehotel.data.repository.RoomRepository
import com.example.dozziehotel.data.repository.ServicePackageRepository
import com.example.dozziehotel.data.repository.SosRepository
import com.example.dozziehotel.data.repository.UserRepository
import org.koin.dsl.module

val repoModule = module {
    single { AuthRepository(get(), get()) }
    single { UserRepository(get()) }
    single { RoomRepository(get()) }
    single { BookingRepository(get(), get()) }
    single { InvoiceRepository(get()) }
    single { PreferenceManager(get(), get()) }
    single { ServicePackageRepository(get()) }
    single { ChatRepository(get()) }
    single { SosRepository(get()) }
}
