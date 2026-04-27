package com.company.app.common.exception

import org.springframework.http.HttpStatus

class AppException(
    val status: HttpStatus,
    val errorCode: String,
    override val message: String
) : RuntimeException(message) {
    companion object {
        fun notFound(message: String = "Resource not found") =
            AppException(HttpStatus.NOT_FOUND, "NOT_FOUND", message)

        fun badRequest(message: String) =
            AppException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", message)

        fun unauthorized(message: String = "Unauthorized") =
            AppException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", message)

        fun forbidden(message: String = "Access denied") =
            AppException(HttpStatus.FORBIDDEN, "FORBIDDEN", message)

        fun conflict(message: String) =
            AppException(HttpStatus.CONFLICT, "CONFLICT", message)

        fun paymentRequired(message: String = "Active subscription required") =
            AppException(HttpStatus.PAYMENT_REQUIRED, "PAYMENT_REQUIRED", message)
    }
}
