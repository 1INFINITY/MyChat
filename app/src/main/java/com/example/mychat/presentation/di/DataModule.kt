package com.example.mychat.presentation.di

import android.content.Context
import com.example.mychat.data.repository.UserRepositoryImpl
import com.example.mychat.data.storage.firebase.FireBaseStorageImpl
import com.example.mychat.data.storage.firebase.FireBaseUserStorage
import com.example.mychat.data.storage.sharedPrefs.SharedPreferencesStorage
import com.example.mychat.data.storage.sharedPrefs.SharedPreferencesStorageImpl
import com.example.mychat.domain.repository.UserRepository
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides

@Module
class DataModule {

    @Provides
    fun provideFireBaseUserStorage(): FireBaseUserStorage {
        return FireBaseStorageImpl(firestoreDb = Firebase.firestore)
    }

    @Provides
    fun provideSharedPreferencesStorage(appContext: Context): SharedPreferencesStorage {
        return SharedPreferencesStorageImpl(appContext = appContext)
    }

    @Provides
    fun provideUserRepository(
        fireBaseUserStorage: FireBaseUserStorage,
        sharedPreferencesStorage: SharedPreferencesStorage,
    ): UserRepository {
        return UserRepositoryImpl(
            firebaseStorage = fireBaseUserStorage,
            sharedPrefsStorage = sharedPreferencesStorage)
    }
}