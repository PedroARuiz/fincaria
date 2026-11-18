package com.fincaria.farms.application.service

import com.fincaria.farms.application.port.`in`.GetUserFarmsUseCase
import com.fincaria.farms.application.port.out.FarmRepository
import com.fincaria.farms.domain.CropType
import com.fincaria.farms.domain.Farm
import com.fincaria.shared.application.PagedResult
import com.fincaria.shared.application.PageRequest
import com.fincaria.shared.application.SortDirection
import org.springframework.stereotype.Service
import kotlin.math.ceil

@Service
class GetUserFarmsService(
    private val farmRepository: FarmRepository,
) : GetUserFarmsUseCase {
    override fun invoke(
        userId: String,
        cropType: CropType?,
        pageRequest: PageRequest,
    ): PagedResult<Farm> {
        val filtered = sorted(
            farmRepository.getUserFarms(userId).filter { farm -> cropType == null || cropType in farm.cropTypes },
            pageRequest,
        )

        val fromIndex = (pageRequest.page * pageRequest.size).coerceIn(0, filtered.size)
        val toIndex = (fromIndex + pageRequest.size).coerceIn(fromIndex, filtered.size)

        return PagedResult(
            content = filtered.subList(fromIndex, toIndex),
            page = pageRequest.page,
            size = pageRequest.size,
            totalElements = filtered.size.toLong(),
            totalPages = if (pageRequest.size == 0) 0 else ceil(filtered.size.toDouble() / pageRequest.size).toInt(),
        )
    }

    private fun sorted(farms: List<Farm>, pageRequest: PageRequest): List<Farm> {
        val sortBy = pageRequest.sortBy ?: return farms
        val comparator = when (sortBy) {
            "cadastralReference" -> compareBy<Farm> { it.cadastralReference.value }
            else -> throw IllegalArgumentException("Unsupported sort field: $sortBy")
        }
        return farms.sortedWith(if (pageRequest.sortDirection == SortDirection.DESC) comparator.reversed() else comparator)
    }
}
