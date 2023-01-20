package com.example.mychat.presentation.di

import android.content.Context
import com.example.mychat.presentation.app.App
import com.example.mychat.presentation.view.*
import com.example.mychat.presentation.viewmodels.ViewModelFactory
import dagger.BindsInstance
import dagger.Component
import dagger.Provides

@Component(modules = [DataModule::class, ViewModelModule::class])
interface AppComponent {

    fun inject(app: App)

    fun inject(userFragment: UserFragment)
    fun inject(chatFragment: ChatFragment)
    fun inject(selectUserFragment: SelectUserFragment)
    fun inject(signUpFragment: SignUpFragment)
    fun inject(signInFragment: SignInFragment)

    fun getViewModelFactory(): ViewModelFactory

    @Component.Factory
    interface AppCompFactory {
        fun create(@BindsInstance appContext: Context): AppComponent
    }
}