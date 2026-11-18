package com.fincaria.farms.application.service

import com.fincaria.farms.application.port.`in`.GetUserFarmsUseCase
import com.fincaria.farms.application.port.out.FarmRepository
import com.fincaria.farms.domain.CropType
import com.fincaria.farms.domain.Farm
import org.springframework.stereotype.Service

@Service
class GetUserFarmsService(
    private val farmRepository: FarmRepository,
) : GetUserFarmsUseCase {
    override fun invoke(
        userId: String,
        cropType: CropType?,
    ): List<Farm> =
        farmRepository.getUserFarms(userId).filter { farm ->
            cropType == null || cropType in farm.cropTypes
        }
}
