package com.fincaria.farms.application.port.out

import com.fincaria.farms.domain.CadastralReference
import com.fincaria.farms.domain.Farm

interface FarmRepository {
    fun save(farm: Farm): Farm
    fun existsByCadastralReference(cadastralReference: CadastralReference): Boolean
    fun getUserFarms(userId: String): List<Farm>
}