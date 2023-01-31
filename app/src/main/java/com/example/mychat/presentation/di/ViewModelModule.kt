package com.example.mychat.presentation.di

import androidx.lifecycle.ViewModel
import com.example.mychat.domain.models.Chat
import com.example.mychat.domain.repository.UserRepository
import com.example.mychat.domain.usecase.*
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
    fun provideSignUpViewModel(userRegistrationUseCase: UserRegistrationUseCase): ViewModel {
        return SignUpViewModel(userRegistrationUseCase = userRegistrationUseCase)
    }

    @IntoMap
    @ViewModelKey(SignInViewModel::class)
    @Provides
    fun provideSignInViewModel(
        userAuthorizationUseCase: UserAuthorizationUseCase,
    ): ViewModel {
        return SignInViewModel(userAuthorizationUseCase = userAuthorizationUseCase)
    }

    @IntoMap
    @ViewModelKey(UserViewModel::class)
    @Provides
    fun provideUserViewModel(
        getCachedUserUseCase: GetCachedUserUseCase,
        observeChatsUseCase: ObserveChatsUseCase,
        signOutUseCase: SignOutUseCase,
    ): ViewModel {
        return UserViewModel(
            getCachedUserUseCase = getCachedUserUseCase,
            observeChatsUseCase = observeChatsUseCase,
            signOutUseCase = signOutUseCase,
        )
    }

    @IntoMap
    @ViewModelKey(SelectUserViewModel::class)
    @Provides
    fun provideSelectUserViewModel(
        getCachedUserUseCase: GetCachedUserUseCase,
        uploadUserListUseCase: UploadUserListUseCase,
        createNewChatUseCase: CreateNewChatUseCase,
    ): ViewModel {
        return SelectUserViewModel(
            getCachedUserUseCase = getCachedUserUseCase,
            uploadUserListUseCase = uploadUserListUseCase,
            createNewChatUseCase = createNewChatUseCase
        )
    }

    @IntoMap
    @ViewModelKey(ChatViewModel::class)
    @Provides
    fun provideChatViewModel(
        getCachedUserUseCase: GetCachedUserUseCase,
        loadChatUseCase: LoadChatUseCase,
        observeChatUseCase: ObserveChatUseCase,
        sendMessageUseCase: SendMessageUseCase,
        changeMessageUseCase: ChangeMessageUseCase,
        deleteMessageUseCase: DeleteMessageUseCase,
    ): ViewModel {
        return ChatViewModel(
            getCachedUserUseCase = getCachedUserUseCase,
            loadChatUseCase = loadChatUseCase,
            observeChatUseCase = observeChatUseCase,
            sendMessageUseCase = sendMessageUseCase,
            changeMessageUseCase = changeMessageUseCase,
            deleteMessageUseCase = deleteMessageUseCase)
    }
}