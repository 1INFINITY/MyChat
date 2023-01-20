package com.example.mychat.presentation.di

import androidx.lifecycle.ViewModel
import com.example.mychat.domain.models.Chat
import com.example.mychat.domain.repository.UserRepository
import com.example.mychat.presentation.viewmodels.*
import dagger.MapKey
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import javax.inject.Provider
import kotlin.reflect.KClass

@MapKey
@Retention(AnnotationRetention.RUNTIME)
annotation class ViewModelKey(val value: KClass<out ViewModel>)

@Module
class ViewModelModule {

//    @Provides
//    fun provideViewModelFactory(viewModelProviders: Map<Class<out ViewModel>, Provider<ViewModel>>): ViewModelFactory {
//        return ViewModelFactory(viewModelProviders = viewModelProviders)
//    }


    @IntoMap
    @ViewModelKey(SignUpViewModel::class)
    @Provides
    fun provideSignUpViewModel(repository: UserRepository): ViewModel {
        return SignUpViewModel(repository = repository)
    }

    @IntoMap
    @ViewModelKey(SignInViewModel::class)
    @Provides
    fun provideSignInViewModel(repository: UserRepository): ViewModel {
        return SignInViewModel(repository = repository)
    }

    @IntoMap
    @ViewModelKey(UserViewModel::class)
    @Provides
    fun provideUserViewModel(repository: UserRepository): ViewModel {
        return UserViewModel(repository = repository)
    }

    @IntoMap
    @ViewModelKey(SelectUserViewModel::class)
    @Provides
    fun provideSelectUserViewModel(repository: UserRepository): ViewModel {
        return SelectUserViewModel(repository = repository)
    }

    @IntoMap
    @ViewModelKey(ChatViewModel::class)
    @Provides
    fun provideChatViewModel(repository: UserRepository): ViewModel {
        return ChatViewModel(repository = repository)
    }
}