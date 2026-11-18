package com.fincaria.farms.infrastructure.`in`.web

import com.fincaria.farms.application.port.`in`.CreateFarmCommand
import com.fincaria.farms.application.port.`in`.CreateFarmUseCase
import com.fincaria.farms.application.port.`in`.GetUserFarmsUseCase
import com.fincaria.farms.domain.CadastralReference
import com.fincaria.farms.domain.CropType
import jakarta.validation.Valid
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
    ): ResponseEntity<List<FarmResponse>> {
        val farms = getUserFarmsUseCase(
            userId = userId,
            cropType = cropType?.let { CropType.valueOf(it) },
        )
        val response = farms.map { farm ->
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
