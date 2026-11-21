package com.fincaria.farms.domain

data class Farm(
    val id: String,
    val userId: String,
    val cadastralReference: CadastralReference,
    val cropTypes: Set<CropType>,
    val cadastralVerification: CadastralVerification,
) {
    init {
        require(cropTypes.isNotEmpty()) { "cropTypes must not be empty" }
    }
}
