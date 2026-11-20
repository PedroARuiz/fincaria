package com.fincaria.auth.infrastructure.`in`.web

import com.fincaria.PostgreSQLTestContainer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.web.servlet.assertj.MockMvcTester
import org.springframework.web.context.WebApplicationContext
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper
import java.util.UUID

@SpringBootTest
class AuthControllerTest(
    @Autowired context: WebApplicationContext,
) : PostgreSQLTestContainer() {
    private val mockMvc = MockMvcTester.from(context) { it.apply<DefaultMockMvcBuilder>(springSecurity()); it.build() }

    private fun registerUser(email: String, password: String) {
        mockMvc.post().uri("/api/v1/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"name":"Pedro","email":"$email","password":"$password"}""")
            .exchange()
    }

    @Test
    fun `logging in with correct credentials returns 200 with a token`() {
        val email = "login-${UUID.randomUUID()}@example.com"
        registerUser(email, "correct-password")

        val result = mockMvc.post().uri("/api/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"email":"$email","password":"correct-password"}""")
            .exchange()

        assertThat(result)
            .hasStatus(200)
            .bodyJson()
            .extractingPath("$.expiresInSeconds").isEqualTo(3600)
        val token = ObjectMapper().readTree(result.response.contentAsString).get("token").asText()
        assertThat(token).isNotBlank()
    }

    @Test
    fun `logging in with the wrong password returns 401`() {
        val email = "login-${UUID.randomUUID()}@example.com"
        registerUser(email, "correct-password")

        val result = mockMvc.post().uri("/api/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"email":"$email","password":"wrong-password"}""")
            .exchange()

        assertThat(result)
            .hasStatus(401)
            .bodyJson()
            .extractingPath("$.message").isEqualTo("Invalid credentials")
    }

    @Test
    fun `logging in with an email that does not exist returns 401`() {
        val result = mockMvc.post().uri("/api/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"email":"missing-${UUID.randomUUID()}@example.com","password":"whatever-password"}""")
            .exchange()

        assertThat(result)
            .hasStatus(401)
            .bodyJson()
            .extractingPath("$.message").isEqualTo("Invalid credentials")
    }

    @Test
    fun `logging in with a blank password returns 400 with the validation error`() {
        val email = "login-${UUID.randomUUID()}@example.com"
        registerUser(email, "correct-password")

        val result = mockMvc.post().uri("/api/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"email":"$email","password":""}""")
            .exchange()

        assertThat(result)
            .hasStatus(400)
            .bodyJson()
            .extractingPath("$.message").isEqualTo("password: must not be blank")
    }
}
