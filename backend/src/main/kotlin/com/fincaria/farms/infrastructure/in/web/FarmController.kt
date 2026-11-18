package com.fincaria.farms.infrastructure.`in`.web

import com.fincaria.farms.application.port.`in`.CreateFarmCommand
import com.fincaria.farms.application.port.`in`.CreateFarmUseCase
import com.fincaria.farms.application.port.`in`.GetUserFarmsUseCase
import com.fincaria.farms.domain.CadastralReference
import com.fincaria.farms.domain.CropType
import com.fincaria.shared.infrastructure.`in`.web.PagedResponse
import com.fincaria.shared.infrastructure.`in`.web.toPageRequest
import com.fincaria.shared.infrastructure.`in`.web.toResponse
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class FarmController(
    private val createFarmUseCase: CreateFarmUseCase,
    private val getUserFarmsUseCase: GetUserFarmsUseCase,
) {
    @PostMapping("/api/v1/users/{userId}/farms")
    fun createFarm(
        @PathVariable userId: String,
        @Valid @RequestBody request: CreateFarmRequest,
    ): ResponseEntity<FarmResponse> {
        val command = CreateFarmCommand(
            userId = userId,
            cadastralReference = CadastralReference(request.cadastralReference),
            cropTypes = request.cropTypes.map { CropType.valueOf(it) }.toSet(),
        )
        val farm = createFarmUseCase(command)
        val response = FarmResponse(
            id = farm.id,
            userId = farm.userId,
            cadastralReference = farm.cadastralReference.value,
            cropTypes = farm.cropTypes.map { it.name }.toSet(),
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping("/api/v1/users/{userId}/farms")
    fun getUserFarms(
        @PathVariable userId: String,
        @RequestParam(required = false) cropType: String?,
        @PageableDefault(size = 20) pageable: Pageable,
    ): ResponseEntity<PagedResponse<FarmResponse>> {
        val pagedFarms = getUserFarmsUseCase(
            userId = userId,
            cropType = cropType?.let { CropType.valueOf(it) },
            pageRequest = pageable.toPageRequest(),
        )
        val response = pagedFarms.toResponse { farm ->
            FarmResponse(
                id = farm.id,
                userId = farm.userId,
                cadastralReference = farm.cadastralReference.value,
                cropTypes = farm.cropTypes.map { it.name }.toSet(),
            )
        }
        return ResponseEntity.status(HttpStatus.OK).body(response)
    }
}
