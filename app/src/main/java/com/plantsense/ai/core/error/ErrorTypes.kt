package com.plantsense.ai.core.error

import androidx.annotation.StringRes
import com.plantsense.ai.R
import com.plantsense.ai.core.network.NetworkResult
import java.io.IOException

sealed interface ErrorType {
    object Network : ErrorType
    object Api : ErrorType
    object Unknown : ErrorType
}

data class UiError(
    @StringRes val messageResId: Int,
    val errorType: ErrorType,
    val fallbackMessage: String? = null
)

object ErrorMapper {
    fun mapToUiError(throwable: Throwable): UiError {
        return when (throwable) {
            is IOException -> UiError(
                messageResId = R.string.network_error_msg,
                errorType = ErrorType.Network
            )
            else -> UiError(
                messageResId = R.string.unknown_error_msg,
                errorType = ErrorType.Unknown,
                fallbackMessage = throwable.localizedMessage
            )
        }
    }

    fun mapToUiError(apiError: NetworkResult.ApiError): UiError {
        return UiError(
            messageResId = R.string.api_error_msg,
            errorType = ErrorType.Api,
            fallbackMessage = apiError.message
        )
    }

    fun mapToUiError(networkError: NetworkResult.NetworkError): UiError {
        return UiError(
            messageResId = R.string.network_error_msg,
            errorType = ErrorType.Network,
            fallbackMessage = networkError.exception.localizedMessage
        )
    }

    fun mapToUiError(unknownError: NetworkResult.UnknownError): UiError {
        return UiError(
            messageResId = R.string.unknown_error_msg,
            errorType = ErrorType.Unknown,
            fallbackMessage = unknownError.throwable.localizedMessage
        )
    }
}
