package com.fincaria.farms.application.service

import com.fincaria.farms.application.port.`in`.GetUserFarmsUseCase
import com.fincaria.farms.application.port.out.FarmRepository
import com.fincaria.farms.domain.CropType
import com.fincaria.farms.domain.Farm
import com.fincaria.shared.application.PagedResult
import com.fincaria.shared.application.PageRequest
import org.springframework.stereotype.Service

@Service
class GetUserFarmsService(
    private val farmRepository: FarmRepository,
) : GetUserFarmsUseCase {
    override fun invoke(
        userId: String,
        cropType: CropType?,
        pageRequest: PageRequest,
    ): PagedResult<Farm> {
        // Validate the sort field here (application policy) so an unknown field
        // becomes a clean 400 instead of leaking a raw property name into the
        // ORDER BY and failing at the database with a 500.
        requireSupportedSortField(pageRequest.sortBy)
        return farmRepository.getUserFarms(userId = userId, cropType = cropType, pageRequest = pageRequest)
    }

    private fun requireSupportedSortField(sortBy: String?) {
        require(sortBy == null || sortBy in SUPPORTED_SORT_FIELDS) {
            "Unsupported sort field: $sortBy"
        }
    }

    private companion object {
        val SUPPORTED_SORT_FIELDS = setOf("cadastralReference")
    }
}
