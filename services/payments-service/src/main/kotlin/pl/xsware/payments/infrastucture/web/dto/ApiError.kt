package pl.xsware.payments.infrastucture.web.dto

import java.time.Instant

data class ApiError(
    val errorCode: String,
    val message: String,
    val path: String,
    val timestamp: Instant = Instant.now()
)
