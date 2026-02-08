package pl.xsware.payments.infrastucture.web.handler

import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.NoHandlerFoundException
import pl.xsware.payments.infrastucture.web.dto.ApiError

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(NoHandlerFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleNotFound(ex: NoHandlerFoundException, request: HttpServletRequest): ApiError {
        return ApiError(
            errorCode = "NOT_FOUND",
            message = ex.message ?: "Endpoint not found",
            path = request.requestURI
        )
    }

    @ExceptionHandler(IllegalArgumentException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleBadRequest(ex: IllegalArgumentException, request: HttpServletRequest): ApiError {
        return ApiError(
            errorCode = "BAD_REQUEST",
            message = ex.message ?: "Invalid request",
            path = request.requestURI
        )
    }
}
