package com.example.mychat.presentation.di

import com.example.mychat.domain.repository.UserRepository
import com.example.mychat.domain.usecase.*
import dagger.Module
import dagger.Provides

@Module
class DomainModule {

    @Provides
    fun provideGetCachedUserUseCase(userRepository: UserRepository): GetCachedUserUseCase{
        return GetCachedUserUseCase(repository = userRepository)
    }
    @Provides
    fun provideChangeMessageUseCase(userRepository: UserRepository): ChangeMessageUseCase{
        return ChangeMessageUseCase(repository = userRepository)
    }
    @Provides
    fun provideDeleteMessageUseCase(userRepository: UserRepository): DeleteMessageUseCase{
        return DeleteMessageUseCase(repository = userRepository)
    }
    @Provides
    fun provideLoadChatUseCase(userRepository: UserRepository): LoadChatUseCase{
        return LoadChatUseCase(repository = userRepository)
    }
    @Provides
    fun provideObserveChatUseCase(userRepository: UserRepository): ObserveChatUseCase{
        return ObserveChatUseCase(repository = userRepository)
    }
    @Provides
    fun provideSendMessageUseCase(userRepository: UserRepository): SendMessageUseCase{
        return SendMessageUseCase(repository = userRepository)
    }
    @Provides
    fun provideUploadUserListUseCase(userRepository: UserRepository): UploadUserListUseCase{
        return UploadUserListUseCase(repository = userRepository)
    }
    @Provides
    fun provideUserAuthorizationUseCase(userRepository: UserRepository): UserAuthorizationUseCase{
        return UserAuthorizationUseCase(repository = userRepository)
    }
    @Provides
    fun provideUserRegistrationUseCase(userRepository: UserRepository): UserRegistrationUseCase{
        return UserRegistrationUseCase(repository = userRepository)
    }
    @Provides
    fun provideObserveChatsUseCase(userRepository: UserRepository): ObserveChatsUseCase{
        return ObserveChatsUseCase(repository = userRepository)
    }
    @Provides
    fun provideSignOutUseCase(userRepository: UserRepository): SignOutUseCase{
        return SignOutUseCase(repository = userRepository)
    }
}