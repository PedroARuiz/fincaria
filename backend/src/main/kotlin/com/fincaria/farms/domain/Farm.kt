package com.fincaria.farms.domain

data class Farm(
    val id: String,
    val userId: String,
    val cadastralReference: CadastralReference,
    val cropType: CropType,
)