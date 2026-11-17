package com.fincaria.farms.infrastructure.`in`.web

import com.fincaria.PostgreSQLTestContainer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.assertj.MockMvcTester
import org.springframework.web.context.WebApplicationContext
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper
import java.util.UUID

@SpringBootTest
class FarmControllerTest(
    @Autowired context: WebApplicationContext,
) : PostgreSQLTestContainer() {
    private val mockMvc = MockMvcTester.from(context)

    @Test
    fun `creating a farm with valid data returns 201 with the created farm`() {
        val userId = mockMvc.post().uri("/api/v1/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"name":"Pedro","email":"pedro-${UUID.randomUUID()}@example.com"}""")
            .exchange()
            .let { userResult ->
                ObjectMapper().readTree(userResult.response.contentAsString).get("id").asText()
            }

        val result = mockMvc.post().uri("/api/v1/users/$userId/farms")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"cadastralReference":"7837301VG8173B0001TT","cropTypes":["OLIVE"]}""")
            .exchange()

        assertThat(result)
            .hasStatus(201)
    }

    @Test
    fun `creating a farm with an invalid cadastral reference returns 400`() {
        val userId = mockMvc.post().uri("/api/v1/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"name":"Pedro","email":"pedro-${UUID.randomUUID()}@example.com"}""")
            .exchange()
            .let { userResult ->
                ObjectMapper().readTree(userResult.response.contentAsString).get("id").asText()
            }

        val result = mockMvc.post().uri("/api/v1/users/$userId/farms")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"cadastralReference":"7837301VG8173B0001T","cropTypes":["OLIVE"]}""")
            .exchange()

        assertThat(result)
            .hasStatus(400)
    }

    @Test
    fun `creating a farm with a cadastral reference already in use returns 409`() {
        val userId = mockMvc.post().uri("/api/v1/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"name":"Pedro","email":"pedro-${UUID.randomUUID()}@example.com"}""")
            .exchange()
            .let { userResult ->
                ObjectMapper().readTree(userResult.response.contentAsString).get("id").asText()
            }

        val result = mockMvc.post().uri("/api/v1/users/$userId/farms")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"cadastralReference":"4A08169P03PRAT0001LR","cropTypes":["OLIVE"]}""")
            .let {
                it.exchange()
                it.exchange()
            }

        assertThat(result)
            .hasStatus(409)
    }

    @Test
    fun `listing farms for a user returns 200 with the user's farms`() {
        val userId = mockMvc.post().uri("/api/v1/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"name":"Pedro","email":"pedro-${UUID.randomUUID()}@example.com"}""")
            .exchange()
            .let { userResult ->
                ObjectMapper().readTree(userResult.response.contentAsString).get("id").asText()
            }

        mockMvc.post().uri("/api/v1/users/$userId/farms")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"cadastralReference":"1234567AB1234C0002PD","cropTypes":["OLIVE"]}""")
            .exchange()

        val result = mockMvc.get().uri("/api/v1/users/$userId/farms")
            .contentType(MediaType.APPLICATION_JSON)
            .exchange()

        assertThat(result)
            .hasStatus(200)
            .bodyJson()
            .extractingPath("$")
            .asArray()
            .hasSize(1)
    }

    @Test
    fun `creating a farm for a user that does not exist returns 400`() {
        val userId = UUID.randomUUID().toString()

        val result = mockMvc.post().uri("/api/v1/users/$userId/farms")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"cadastralReference":"9999999ZZ9999Z0004SA","cropTypes":["OLIVE"]}""")
            .exchange()

        assertThat(result)
            .hasStatus(400)
    }

    @Test
    fun `creating a farm with no crop types returns 400`() {
        val userId = mockMvc.post().uri("/api/v1/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"name":"Pedro","email":"pedro-${UUID.randomUUID()}@example.com"}""")
            .exchange()
            .let { userResult ->
                ObjectMapper().readTree(userResult.response.contentAsString).get("id").asText()
            }

        val result = mockMvc.post().uri("/api/v1/users/$userId/farms")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"cadastralReference":"9637302VG8173B0003SU","cropTypes":[]}""")
            .exchange()

        assertThat(result)
            .hasStatus(400)
    }

    @Test
    fun `listing farms for a user with no farms returns 200 with an empty list`() {
        val userId = mockMvc.post().uri("/api/v1/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"name":"Pedro","email":"pedro-${UUID.randomUUID()}@example.com"}""")
            .exchange()
            .let { userResult ->
                ObjectMapper().readTree(userResult.response.contentAsString).get("id").asText()
            }

        val result = mockMvc.get().uri("/api/v1/users/$userId/farms")
            .contentType(MediaType.APPLICATION_JSON)
            .exchange()

        assertThat(result)
            .hasStatus(200)
            .bodyJson()
            .extractingPath("$")
            .asArray()
            .hasSize(0)
    }
}
