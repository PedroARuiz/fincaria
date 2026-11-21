package com.fincaria.farms.application.service

import com.fincaria.farms.application.port.`in`.CreateFarmCommand
import com.fincaria.farms.application.port.`in`.CreateFarmUseCase
import com.fincaria.farms.application.port.out.CadastralRegistry
import com.fincaria.farms.application.port.out.FarmRepository
import com.fincaria.farms.domain.CadastralVerification
import com.fincaria.farms.domain.Farm
import com.fincaria.farms.domain.exception.DuplicatedCadastralReferenceException
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class CreateFarmService(
    private val farmRepository: FarmRepository,
    private val cadastralRegistry: CadastralRegistry,
) : CreateFarmUseCase {
    override fun invoke(command: CreateFarmCommand): Farm {
        if (farmRepository.existsByCadastralReference(command.cadastralReference)) {
            throw DuplicatedCadastralReferenceException(command.cadastralReference)
        }
        val details = cadastralRegistry.lookup(command.cadastralReference)
        val verification = details
            ?.let { CadastralVerification.Verified(it) }
            ?: CadastralVerification.Unverified
        return farmRepository.save(
            farm = Farm(
                id = UUID.randomUUID().toString(),
                userId = command.userId,
                cadastralReference = command.cadastralReference,
                cropTypes = command.cropTypes,
                cadastralVerification = verification,
            ),
        )
    }
}
