package com.fincaria.users.domain.exception

class EmailAlreadyInUseException(email: String) : RuntimeException("Email already in use: $email")
