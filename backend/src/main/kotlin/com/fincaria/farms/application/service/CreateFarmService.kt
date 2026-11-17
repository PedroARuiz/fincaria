package com.fincaria.farms.application.service

import com.fincaria.farms.application.port.`in`.CreateFarmCommand
import com.fincaria.farms.application.port.`in`.CreateFarmUseCase
import com.fincaria.farms.application.port.out.FarmRepository
import com.fincaria.farms.domain.Farm
import com.fincaria.farms.domain.exception.DuplicatedCadastralReferenceException
import org.springframework.stereotype.Service
import java.util.*

@Service
class CreateFarmService(
    private val farmRepository: FarmRepository,
) : CreateFarmUseCase {
    override fun invoke(command: CreateFarmCommand): Farm =
        if (farmRepository.existsByCadastralReference(command.cadastralReference)) {
            throw DuplicatedCadastralReferenceException(command.cadastralReference)
        } else {
            farmRepository.save(
                farm = Farm(
                    id = UUID.randomUUID().toString(),
                    userId = command.userId,
                    cadastralReference = command.cadastralReference,
                    cropTypes = command.cropTypes,
                )
            )
        }
}