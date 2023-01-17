package com.example.mychat.presentation.app

import android.app.Application
import android.util.Log
import com.example.mychat.domain.repository.UserRepository
import com.example.mychat.presentation.di.AppComponent
import com.example.mychat.presentation.di.DaggerAppComponent
import com.example.mychat.presentation.di.DataModule
import javax.inject.Inject

class App: Application() {

    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        appComponent =
            DaggerAppComponent.factory().create(appContext = this)
    }
}