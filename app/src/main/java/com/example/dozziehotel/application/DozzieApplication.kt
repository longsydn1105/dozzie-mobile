package com.example.dozziehotel.application

import android.app.Application
import com.example.dozziehotel.di.databaseModule
import com.example.dozziehotel.di.networkModule
import com.example.dozziehotel.di.repoModule
import com.example.dozziehotel.di.viewModelModule
import com.google.firebase.FirebaseApp
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class DozzieApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        startKoin {
            androidContext(this@DozzieApplication)
            modules(
                networkModule,
                databaseModule,
                repoModule,
                viewModelModule
            )
        }
    }
}
