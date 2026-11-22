package com.fincaria.farms.infrastructure.`in`.web

import com.fincaria.PostgreSQLTestContainer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.web.servlet.assertj.MockMvcTester
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.web.context.WebApplicationContext
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper
import java.util.UUID

@SpringBootTest
class CropTypeControllerTest(
    @Autowired context: WebApplicationContext,
) : PostgreSQLTestContainer() {
    private val mockMvc = MockMvcTester.from(context) { it.apply<DefaultMockMvcBuilder>(springSecurity()); it.build() }

    /** Registers a new user and logs in, returning a valid Bearer token for them. */
    private fun registerAndLogIn(): String {
        val email = "pedro-${UUID.randomUUID()}@example.com"
        val password = "super-secret"

        mockMvc.post().uri("/api/v1/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"name":"Pedro","email":"$email","password":"$password"}""")
            .exchange()

        return mockMvc.post().uri("/api/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"email":"$email","password":"$password"}""")
            .exchange()
            .let { ObjectMapper().readTree(it.response.contentAsString).get("token").asText() }
    }

    @Test
    fun `getting the crop type catalog returns 200 with the seeded entries`() {
        val token = registerAndLogIn()

        val result = mockMvc.get().uri("/api/v1/crop-types")
            .header("Authorization", "Bearer $token")
            .exchange()

        assertThat(result)
            .hasStatus(200)
            .bodyJson()
            .extractingPath("$[0].code")
            .isEqualTo("OLIVE")
        assertThat(result)
            .bodyJson()
            .extractingPath("$[0].label")
            .asString()
            .isNotEmpty()
    }

    @Test
    fun `getting the crop type catalog without authentication returns 401`() {
        val result = mockMvc.get().uri("/api/v1/crop-types").exchange()

        assertThat(result).hasStatus(401)
    }

    @Test
    fun `catalog cache hit-miss metrics are exposed on the Prometheus endpoint`() {
        val token = registerAndLogIn()
        // Two reads so the cache both populates (miss) and serves (hit).
        repeat(2) {
            mockMvc.get().uri("/api/v1/crop-types")
                .header("Authorization", "Bearer $token")
                .exchange()
        }

        val metrics = mockMvc.get().uri("/actuator/prometheus").exchange().response.contentAsString

        assertThat(metrics).contains("cache_gets_total")
        assertThat(metrics).contains("cache=\"cropTypeCatalog\"")
    }
}
