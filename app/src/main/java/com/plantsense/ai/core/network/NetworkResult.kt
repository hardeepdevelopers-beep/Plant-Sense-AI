package com.plantsense.ai.core.network

import java.io.IOException

sealed interface NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>
    data class ApiError(val code: Int, val message: String) : NetworkResult<Nothing>
    data class NetworkError(val exception: IOException) : NetworkResult<Nothing>
    data class UnknownError(val throwable: Throwable) : NetworkResult<Nothing>
}
