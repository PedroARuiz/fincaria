package com.fincaria.shared.infrastructure.`in`.web.security

import com.fincaria.auth.application.port.out.TokenParser
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Reads the `Authorization: Bearer <token>` header, if present, and tries to establish an
 * authenticated principal (the token's userId) from it.
 *
 * This filter never rejects a request itself, whether the header is missing or the token is
 * invalid/expired — it only ever adds an authenticated principal when it can. Deciding whether
 * a given endpoint actually requires authentication is left entirely to the authorization rules
 * in [SecurityConfig] and the resulting [RestAuthenticationEntryPoint], so that logic lives in a
 * single place instead of being duplicated here.
 */
@Component
class JwtAuthenticationFilter(
    private val tokenParser: TokenParser,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val header = request.getHeader("Authorization")
        if (header != null && header.startsWith("Bearer ")) {
            val token = header.removePrefix("Bearer ")
            runCatching { tokenParser.parse(token) }
                .onSuccess { userId ->
                    SecurityContextHolder.getContext().authentication =
                        UsernamePasswordAuthenticationToken(userId, null, emptyList())
                }
        }
        filterChain.doFilter(request, response)
    }
}
