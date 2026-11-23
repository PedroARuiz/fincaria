package com.fincaria.farms.infrastructure.out.persistence

import com.fincaria.farms.application.port.out.FarmRepository
import com.fincaria.farms.domain.CadastralDetails
import com.fincaria.farms.domain.CadastralReference
import com.fincaria.farms.domain.CadastralVerification
import com.fincaria.farms.domain.CropType
import com.fincaria.farms.domain.Farm
import com.fincaria.shared.application.PagedResult
import com.fincaria.shared.application.PageRequest
import com.fincaria.shared.infrastructure.out.persistence.toPageable
import com.fincaria.shared.infrastructure.out.persistence.toPagedResult
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class FarmRepositoryImpl(
    private val farmJpaRepository: FarmJpaRepository,
) : FarmRepository {
    override fun save(farm: Farm): Farm {
        val details = (farm.cadastralVerification as? CadastralVerification.Verified)?.details
        val entity = FarmJpaEntity(
            id = UUID.fromString(farm.id),
            userId = UUID.fromString(farm.userId),
            cadastralReference = farm.cadastralReference.value,
            cropTypes = farm.cropTypes.toMutableSet(),
            verificationStatus = when (farm.cadastralVerification) {
                is CadastralVerification.Verified -> VERIFIED
                CadastralVerification.Unverified -> UNVERIFIED
            },
            cadastralMunicipality = details?.municipality,
            cadastralProvince = details?.province,
            cadastralAreaHectares = details?.areaHectares,
        )
        return farmJpaRepository.save(entity).toDomain()
    }

    override fun existsByCadastralReference(cadastralReference: CadastralReference): Boolean =
        farmJpaRepository.existsByCadastralReference(cadastralReference = cadastralReference.value)

    override fun getUserFarms(userId: String, cropType: CropType?, pageRequest: PageRequest): PagedResult<Farm> {
        val userUuid = UUID.fromString(userId)
        val pageable = pageRequest.toPageable()
        val page = if (cropType == null) {
            farmJpaRepository.findByUserId(userUuid, pageable)
        } else {
            farmJpaRepository.findByUserIdAndCropType(userUuid, cropType, pageable)
        }
        return page.toPagedResult { it.toDomain() }
    }

    private fun FarmJpaEntity.toDomain(): Farm =
        Farm(
            id = id.toString(),
            userId = userId.toString(),
            cadastralReference = CadastralReference(cadastralReference),
            cropTypes = cropTypes.toSet(),
            cadastralVerification = if (verificationStatus == VERIFIED) {
                CadastralVerification.Verified(
                    CadastralDetails(
                        municipality = cadastralMunicipality!!,
                        province = cadastralProvince!!,
                        areaHectares = cadastralAreaHectares!!,
                    ),
                )
            } else {
                CadastralVerification.Unverified
            },
        )

    private companion object {
        const val VERIFIED = "VERIFIED"
        const val UNVERIFIED = "UNVERIFIED"
    }
}
