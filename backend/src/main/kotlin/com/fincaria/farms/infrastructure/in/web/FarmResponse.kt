package com.fincaria.farms.infrastructure.`in`.web

data class FarmResponse(
    val id: String,
    val userId: String,
    val cadastralReference: String,
    val cropTypes: Set<String>,
)
