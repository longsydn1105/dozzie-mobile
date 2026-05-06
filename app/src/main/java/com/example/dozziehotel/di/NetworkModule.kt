package com.example.dozziehotel.di

import com.example.dozziehotel.BuildConfig
import com.example.dozziehotel.data.remote.AuthApi
import com.example.dozziehotel.data.remote.AuthInterceptor
import com.example.dozziehotel.data.remote.BookingApi
import com.example.dozziehotel.data.remote.ChatApi
import com.example.dozziehotel.data.remote.InvoiceApi
import com.example.dozziehotel.data.remote.RoomApi
import com.example.dozziehotel.data.remote.ServicePackageApi
import com.example.dozziehotel.data.remote.SosApi
import com.example.dozziehotel.data.remote.UserApi
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val networkModule = module {
    single {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    single { AuthInterceptor(get()) }

    single{ Gson() }

    single {
        OkHttpClient.Builder()
            .addInterceptor(get<HttpLoggingInterceptor>())
            .addInterceptor(get<AuthInterceptor>())
            .build()
    }

    single {
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(get())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    single<AuthApi> { get<Retrofit>().create(AuthApi::class.java) }
    single<UserApi> { get<Retrofit>().create(UserApi::class.java) }
    single<RoomApi> { get<Retrofit>().create(RoomApi::class.java) }
    single<BookingApi> { get<Retrofit>().create(BookingApi::class.java) }
    single<ServicePackageApi> { get<Retrofit>().create(ServicePackageApi::class.java) }
    single<InvoiceApi> { get<Retrofit>().create(InvoiceApi::class.java) }
    single<ChatApi> { get<Retrofit>().create(ChatApi::class.java) }
    single<SosApi> { get<Retrofit>().create(SosApi::class.java) }
}
