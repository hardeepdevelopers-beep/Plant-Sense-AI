package com.plantsense.ai.core.network

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

suspend fun <T> safeApiCall(
    dispatcher: CoroutineDispatcher,
    apiCall: suspend () -> T
): NetworkResult<T> = withContext(dispatcher) {
    var retryCount = 0
    val maxRetries = 3
    while (true) {
        try {
            return@withContext NetworkResult.Success(apiCall())
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string() ?: ""
            return@withContext NetworkResult.ApiError(e.code(), errorBody.ifBlank { e.message() })
        } catch (e: IOException) {
            if (++retryCount >= maxRetries) {
                return@withContext NetworkResult.NetworkError(e)
            }
            delay(1000L * retryCount)
        } catch (e: Throwable) {
            return@withContext NetworkResult.UnknownError(e)
        }
    }
    @Suppress("UNREACHABLE_CODE")
    NetworkResult.UnknownError(IllegalStateException("Execution failed to complete"))
}
