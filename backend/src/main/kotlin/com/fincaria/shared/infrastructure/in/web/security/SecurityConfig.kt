package com.fincaria.shared.infrastructure.`in`.web.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val restAuthenticationEntryPoint: RestAuthenticationEntryPoint,
    private val restAccessDeniedHandler: RestAccessDeniedHandler,
) {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { authorize ->
                authorize.requestMatchers(HttpMethod.POST, "/api/v1/users").permitAll()
                authorize.requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                authorize.requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                authorize.requestMatchers("/actuator/health").permitAll()
                authorize.anyRequest().fullyAuthenticated()
            }
            .exceptionHandling { exceptions ->
                exceptions.authenticationEntryPoint(restAuthenticationEntryPoint)
                exceptions.accessDeniedHandler(restAccessDeniedHandler)
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
        return http.build()
    }
}
