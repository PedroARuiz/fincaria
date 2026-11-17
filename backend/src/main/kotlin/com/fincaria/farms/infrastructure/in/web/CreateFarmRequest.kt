package com.fincaria.farms.infrastructure.`in`.web

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty

data class CreateFarmRequest(
    @field:NotBlank
    val cadastralReference: String,
    @field:NotEmpty
    val cropTypes: Set<String>,
)
