package com.fincaria.farms.domain.exception

import com.fincaria.farms.domain.CadastralReference

class DuplicatedCadastralReferenceException(
    cadastralReference: CadastralReference,
) : RuntimeException("Cadastral reference already registered: ${cadastralReference.value}")
