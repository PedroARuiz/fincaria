package com.fincaria.farms.application.port.`in`

import com.fincaria.farms.domain.CadastralReference
import com.fincaria.farms.domain.CropType
import com.fincaria.farms.domain.Farm

data class CreateFarmCommand(
    val userId: String,
    val cadastralReference: CadastralReference,
    val cropType: CropType,
)

interface CreateFarmUseCase {
    fun execute(command: CreateFarmCommand): Farm
}