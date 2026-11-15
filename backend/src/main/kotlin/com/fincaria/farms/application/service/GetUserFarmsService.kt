package com.fincaria.farms.application.service

import com.fincaria.farms.application.port.`in`.GetUserFarmsUseCase
import com.fincaria.farms.application.port.out.FarmRepository
import com.fincaria.farms.domain.Farm
import org.springframework.stereotype.Service

@Service
class GetUserFarmsService(
    private val farmRepository: FarmRepository,
) : GetUserFarmsUseCase {
    override fun invoke(userId: String): List<Farm> =
        farmRepository.getUserFarms(userId)
}
