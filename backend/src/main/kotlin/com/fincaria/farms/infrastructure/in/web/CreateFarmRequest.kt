package com.fincaria.farms.infrastructure.`in`.web

import jakarta.validation.constraints.NotBlank

data class CreateFarmRequest(
    @field:NotBlank
    val cadastralReference: String,
    @field:NotBlank
    val cropType: String,
)
