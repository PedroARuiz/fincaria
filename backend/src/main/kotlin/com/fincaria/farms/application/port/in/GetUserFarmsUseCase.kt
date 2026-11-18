package com.fincaria.farms.application.port.`in`

import com.fincaria.farms.domain.CropType
import com.fincaria.farms.domain.Farm
import com.fincaria.shared.application.PagedResult
import com.fincaria.shared.application.PageRequest

interface GetUserFarmsUseCase {
    operator fun invoke(
        userId: String,
        cropType: CropType? = null,
        pageRequest: PageRequest = PageRequest(page = 0, size = 20),
    ): PagedResult<Farm>
}
