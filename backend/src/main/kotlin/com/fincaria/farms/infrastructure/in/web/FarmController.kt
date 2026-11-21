package com.fincaria.farms.infrastructure.`in`.web

import com.fincaria.farms.application.port.`in`.CreateFarmCommand
import com.fincaria.farms.application.port.`in`.CreateFarmUseCase
import com.fincaria.farms.application.port.`in`.GetUserFarmsUseCase
import com.fincaria.farms.domain.CadastralReference
import com.fincaria.farms.domain.CadastralVerification
import com.fincaria.farms.domain.CropType
import com.fincaria.farms.domain.Farm
import com.fincaria.shared.infrastructure.`in`.web.PagedResponse
import com.fincaria.shared.infrastructure.`in`.web.toPageRequest
import com.fincaria.shared.infrastructure.`in`.web.toResponse
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class FarmController(
    private val createFarmUseCase: CreateFarmUseCase,
    private val getUserFarmsUseCase: GetUserFarmsUseCase,
) {
    @PostMapping("/api/v1/farms")
    fun createFarm(
        authentication: Authentication,
        @Valid @RequestBody request: CreateFarmRequest,
    ): ResponseEntity<FarmResponse> {
        val command = CreateFarmCommand(
            userId = authentication.name,
            cadastralReference = CadastralReference(request.cadastralReference),
            cropTypes = request.cropTypes.map { CropType.valueOf(it) }.toSet(),
        )
        val farm = createFarmUseCase(command)
        return ResponseEntity.status(HttpStatus.CREATED).body(farm.toResponse())
    }

    @GetMapping("/api/v1/farms")
    fun getUserFarms(
        authentication: Authentication,
        @RequestParam(required = false) cropType: String?,
        @PageableDefault(size = 20) pageable: Pageable,
    ): ResponseEntity<PagedResponse<FarmResponse>> {
        val pagedFarms = getUserFarmsUseCase(
            userId = authentication.name,
            cropType = cropType?.let { CropType.valueOf(it) },
            pageRequest = pageable.toPageRequest(),
        )
        val response = pagedFarms.toResponse { farm -> farm.toResponse() }
        return ResponseEntity.status(HttpStatus.OK).body(response)
    }

    private fun Farm.toResponse(): FarmResponse {
        val verification = cadastralVerification
        return FarmResponse(
            id = id,
            userId = userId,
            cadastralReference = cadastralReference.value,
            cropTypes = cropTypes.map { it.name }.toSet(),
            verificationStatus = when (verification) {
                is CadastralVerification.Verified -> "VERIFIED"
                CadastralVerification.Unverified -> "UNVERIFIED"
            },
            cadastralDetails = when (verification) {
                is CadastralVerification.Verified -> CadastralDetailsResponse(
                    municipality = verification.details.municipality,
                    province = verification.details.province,
                    areaHectares = verification.details.areaHectares,
                )
                CadastralVerification.Unverified -> null
            },
        )
    }
}
