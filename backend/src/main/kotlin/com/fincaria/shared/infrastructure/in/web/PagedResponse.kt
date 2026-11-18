package com.fincaria.shared.infrastructure.`in`.web

import com.fincaria.shared.application.PageRequest
import com.fincaria.shared.application.PagedResult
import com.fincaria.shared.application.SortDirection
import org.springframework.data.domain.Pageable

data class PagedResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
)

fun <T, R> PagedResult<T>.toResponse(mapper: (T) -> R): PagedResponse<R> =
    PagedResponse(
        content = content.map(mapper),
        page = page,
        size = size,
        totalElements = totalElements,
        totalPages = totalPages,
    )

fun Pageable.toPageRequest(): PageRequest {
    val order = sort.firstOrNull()
    return PageRequest(
        page = pageNumber,
        size = pageSize,
        sortBy = order?.property,
        sortDirection = if (order?.isAscending == false) SortDirection.DESC else SortDirection.ASC,
    )
}
