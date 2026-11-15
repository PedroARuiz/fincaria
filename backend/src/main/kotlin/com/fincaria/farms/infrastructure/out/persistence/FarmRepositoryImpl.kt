package com.fincaria.farms.infrastructure.out.persistence

import com.fincaria.farms.application.port.out.FarmRepository
import com.fincaria.farms.domain.CadastralReference
import com.fincaria.farms.domain.Farm
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class FarmRepositoryImpl(
    private val farmJpaRepository: FarmJpaRepository,
) : FarmRepository {
    override fun save(farm: Farm): Farm {
        val entity = FarmJpaEntity(
            id = UUID.fromString(farm.id),
            userId = UUID.fromString(farm.userId),
            cadastralReference = farm.cadastralReference.value,
            cropType = farm.cropType,
        )
        val saved = farmJpaRepository.save(entity)
        return Farm(
            id = saved.id.toString(),
            userId = saved.userId.toString(),
            cadastralReference = CadastralReference(saved.cadastralReference),
            cropType = saved.cropType,
        )
    }

    override fun existsByCadastralReference(cadastralReference: CadastralReference): Boolean =
        farmJpaRepository.existsByCadastralReference(cadastralReference = cadastralReference.value)

    override fun getUserFarms(userId: String): List<Farm> =
        farmJpaRepository.findByUserId(UUID.fromString(userId)).map { farmJpaEntity ->
            Farm(
                id = farmJpaEntity.id.toString(),
                userId = farmJpaEntity.userId.toString(),
                cadastralReference = CadastralReference(farmJpaEntity.cadastralReference),
                cropType = farmJpaEntity.cropType,
            )
        }
}