package com.example.mychat.domain.repository


sealed class ResultData<out T > {

    data class Success<out T>(val value: T) : ResultData<T>()

    data class Failure<out T>(val message : String) : ResultData<T>()

    data class Loading<out T>(val value: T? = null) : ResultData<T>()

    data class Update<out T>(val value: T): ResultData<T>()

    data class Removed<out T>(val value: T): ResultData<T>()

    companion object {

        fun <T> removed( value: T): ResultData<T> = Removed(value)

        fun <T> update( value: T): ResultData<T> = Update(value)

        fun <T> loading( value: T? ): ResultData<T> = Loading(value)

        fun <T> success( value: T ): ResultData<T> = Success(value)

        fun <T> failure( error_msg : String ): ResultData<T> = Failure(error_msg)

    }

}