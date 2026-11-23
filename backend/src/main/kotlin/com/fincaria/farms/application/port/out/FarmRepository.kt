package com.fincaria.farms.application.port.out

import com.fincaria.farms.domain.CadastralReference
import com.fincaria.farms.domain.CropType
import com.fincaria.farms.domain.Farm
import com.fincaria.shared.application.PageRequest
import com.fincaria.shared.application.PagedResult

interface FarmRepository {
    fun save(farm: Farm): Farm
    fun existsByCadastralReference(cadastralReference: CadastralReference): Boolean
    fun getUserFarms(userId: String, cropType: CropType?, pageRequest: PageRequest): PagedResult<Farm>
}
