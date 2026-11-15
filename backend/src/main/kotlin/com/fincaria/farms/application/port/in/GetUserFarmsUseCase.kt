package com.fincaria.farms.application.port.`in`

import com.fincaria.farms.domain.Farm

interface GetUserFarmsUseCase {
    operator fun invoke(userId: String): List<Farm>
}