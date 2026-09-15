package com.pause.shared

/**
 * Lightweight Result wrapper used across intelligence-layer boundaries.
 */
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String, val cause: Throwable? = null) : Result<Nothing>()

    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw IllegalStateException(message)
    }

    fun isSuccess(): Boolean = this is Success
}
