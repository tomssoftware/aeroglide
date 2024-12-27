package com.alpsfly.aeroglide.core.firebase

sealed class Response<out T> {
    data object Processing: Response<Nothing>()

    data class Success<out T>(
        val data: T
    ): Response<T>()

    data class Error(
        val message: String
    ): Response<Nothing>()
}