package com.fincaria.shared.infrastructure.`in`.web

data class ErrorResponse(
    val status: Int,
    val error: String,
    val message: String,
)
