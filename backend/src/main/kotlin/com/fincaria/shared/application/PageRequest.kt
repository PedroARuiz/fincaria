package com.fincaria.shared.application

enum class SortDirection {
    ASC,
    DESC,
}

data class PageRequest(
    val page: Int,
    val size: Int,
    val sortBy: String? = null,
    val sortDirection: SortDirection = SortDirection.ASC,
)
