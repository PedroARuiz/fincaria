package com.fincaria.farms.infrastructure.`in`.web

data class FarmResponse(
    val id: String,
    val userId: String,
    val cadastralReference: String,
    val cropTypes: Set<String>,
    val verificationStatus: String,
    val cadastralDetails: CadastralDetailsResponse?,
)

data class CadastralDetailsResponse(
    val municipality: String,
    val province: String,
    val areaHectares: Double,
)
