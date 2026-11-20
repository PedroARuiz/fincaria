package com.fincaria.shared.infrastructure.`in`.web.security

import com.fincaria.auth.infrastructure.`in`.web.AuthController
import com.fincaria.users.infrastructure.`in`.web.UserController
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.security.SecurityScheme
import io.swagger.v3.oas.models.security.SecurityRequirement
import org.springdoc.core.customizers.GlobalOperationCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/** Controller methods reachable without a Bearer token — kept out of the security requirement
 * added by [OpenApiSecurityConfig] below, so Swagger UI doesn't show a padlock on them. */
private val PUBLIC_OPERATIONS = setOf(
    UserController::class.java to "createUser",
    AuthController::class.java to "login",
)

@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
)
@Configuration
class OpenApiSecurityConfig {
    @Bean
    fun bearerAuthOperationCustomizer(): GlobalOperationCustomizer =
        GlobalOperationCustomizer { operation, handlerMethod ->
            if ((handlerMethod.beanType to handlerMethod.method.name) !in PUBLIC_OPERATIONS) {
                operation.addSecurityItem(SecurityRequirement().addList("bearerAuth"))
            }
            operation
        }
}
