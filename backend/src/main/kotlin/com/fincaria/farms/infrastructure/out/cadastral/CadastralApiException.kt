package com.fincaria.farms.infrastructure.out.cadastral

/** Raised by the external cadastral client when a lookup call fails. */
class CadastralApiException(message: String) : RuntimeException(message)
