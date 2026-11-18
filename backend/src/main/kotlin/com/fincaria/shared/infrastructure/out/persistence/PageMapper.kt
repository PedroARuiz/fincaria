package com.fincaria.shared.infrastructure.out.persistence

import com.fincaria.shared.application.PageRequest
import com.fincaria.shared.application.PagedResult
import com.fincaria.shared.application.SortDirection
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.domain.PageRequest as SpringDataPageRequest

fun PageRequest.toPageable(): Pageable {
    val sort = sortBy
        ?.let { Sort.by(it) }
        ?.let { if (sortDirection == SortDirection.DESC) it.descending() else it.ascending() }
        ?: Sort.unsorted()
    return SpringDataPageRequest.of(page, size, sort)
}

fun <T : Any, R> Page<T>.toPagedResult(mapper: (T) -> R): PagedResult<R> =
    PagedResult(
        content = content.map(mapper),
        page = number,
        size = size,
        totalElements = totalElements,
        totalPages = totalPages,
    )
