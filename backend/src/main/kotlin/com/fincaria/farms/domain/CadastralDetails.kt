package com.fincaria.farms.domain

/**
 * Cadastral data enriched from an external registry lookup (municipality,
 * province, surface area). Present only when the farm could be verified
 * against the registry; absent when the lookup degraded gracefully.
 */
data class CadastralDetails(
    val municipality: String,
    val province: String,
    val areaHectares: Double,
)
