package com.fincaria.farms.application.service

import com.fincaria.farms.application.port.`in`.CreateFarmCommand
import com.fincaria.farms.application.port.`in`.CreateFarmUseCase
import com.fincaria.farms.application.port.out.CadastralRegistry
import com.fincaria.farms.application.port.out.FarmRepository
import com.fincaria.farms.domain.CadastralVerification
import com.fincaria.farms.domain.Farm
import com.fincaria.farms.domain.FarmRegistered
import com.fincaria.farms.domain.exception.DuplicatedCadastralReferenceException
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
class CreateFarmService(
    private val farmRepository: FarmRepository,
    private val cadastralRegistry: CadastralRegistry,
    private val farmWriter: FarmWriter,
) : CreateFarmUseCase {
    override fun invoke(command: CreateFarmCommand): Farm {
        if (farmRepository.existsByCadastralReference(command.cadastralReference)) {
            throw DuplicatedCadastralReferenceException(command.cadastralReference)
        }
        // The cadastral lookup is an external, retried, circuit-broken network
        // call: run it here, outside the transaction, so no database connection
        // is held open while it waits.
        val details = cadastralRegistry.lookup(command.cadastralReference)
        val verification = details
            ?.let { CadastralVerification.Verified(it) }
            ?: CadastralVerification.Unverified
        val farm = Farm(
            id = UUID.randomUUID().toString(),
            userId = command.userId,
            cadastralReference = command.cadastralReference,
            cropTypes = command.cropTypes,
            cadastralVerification = verification,
        )
        val event = FarmRegistered(
            farmId = farm.id,
            userId = farm.userId,
            cadastralReference = farm.cadastralReference.value,
            cropTypes = farm.cropTypes,
            occurredAt = Instant.now(),
        )
        return farmWriter.save(farm = farm, event = event)
    }
}
