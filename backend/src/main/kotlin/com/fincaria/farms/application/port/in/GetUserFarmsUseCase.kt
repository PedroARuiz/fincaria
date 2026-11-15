package com.fincaria.farms.application.port.`in`

import com.fincaria.farms.domain.Farm

interface GetUserFarmsUseCase {
    fun execute(userId: String): List<Farm>
}