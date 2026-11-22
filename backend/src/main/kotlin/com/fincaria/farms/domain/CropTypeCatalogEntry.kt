package com.fincaria.farms.domain

/**
 * A single entry in the crop type reference catalog: a [CropType] decorated
 * with presentation data (label, description, ordering).
 *
 * The [CropType] enum remains the source of truth for which crop types are
 * valid; this type only carries the reference/presentation attributes stored
 * alongside each code. The [label] and [description] are single-locale for now
 * (see the i18n backlog item in the roadmap).
 */
data class CropTypeCatalogEntry(
    val code: CropType,
    val label: String,
    val description: String?,
    val displayOrder: Int,
)
