package com.fincaria.farms.infrastructure.`in`.web

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
class FarmControllerTest(
    @Autowired context: WebApplicationContext,
) : PostgreSQLTestContainer() {
    private val mockMvc = MockMvcTester.from(context) { it.apply<DefaultMockMvcBuilder>(springSecurity()); it.build() }

    /** Registers a new user and logs in, returning their userId and a valid Bearer token. */
    private fun registerAndLogIn(): Pair<String, String> {
        val email = "pedro-${UUID.randomUUID()}@example.com"
        val password = "super-secret"

        val userId = mockMvc.post().uri("/api/v1/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"name":"Pedro","email":"$email","password":"$password"}""")
            .exchange()
            .let { ObjectMapper().readTree(it.response.contentAsString).get("id").asText() }

        val token = mockMvc.post().uri("/api/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"email":"$email","password":"$password"}""")
            .exchange()
            .let { ObjectMapper().readTree(it.response.contentAsString).get("token").asText() }

        return userId to token
    }

    @Test
    fun `creating a farm with valid data returns 201 with the created farm`() {
        val (userId, token) = registerAndLogIn()

        val result = mockMvc.post().uri("/api/v1/users/$userId/farms")
            .header("Authorization", "Bearer $token")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"cadastralReference":"7837301VG8173B0001TT","cropTypes":["OLIVE"]}""")
            .exchange()

        assertThat(result)
            .hasStatus(201)
    }

    @Test
    fun `creating a farm with an invalid cadastral reference returns 400`() {
        val (userId, token) = registerAndLogIn()

        val result = mockMvc.post().uri("/api/v1/users/$userId/farms")
            .header("Authorization", "Bearer $token")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"cadastralReference":"7837301VG8173B0001T","cropTypes":["OLIVE"]}""")
            .exchange()

        assertThat(result)
            .hasStatus(400)
    }

    @Test
    fun `creating a farm with a cadastral reference already in use returns 409`() {
        val (userId, token) = registerAndLogIn()

        val result = mockMvc.post().uri("/api/v1/users/$userId/farms")
            .header("Authorization", "Bearer $token")
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
        val (userId, token) = registerAndLogIn()

        mockMvc.post().uri("/api/v1/users/$userId/farms")
            .header("Authorization", "Bearer $token")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"cadastralReference":"1234567AB1234C0002PD","cropTypes":["OLIVE"]}""")
            .exchange()

        val result = mockMvc.get().uri("/api/v1/users/$userId/farms")
            .header("Authorization", "Bearer $token")
            .contentType(MediaType.APPLICATION_JSON)
            .exchange()

        assertThat(result)
            .hasStatus(200)
            .bodyJson()
            .extractingPath("$.content")
            .asArray()
            .hasSize(1)
    }

    @Test
    fun `creating a farm for a user that does not exist returns 400`() {
        val (_, token) = registerAndLogIn()
        val nonExistentUserId = UUID.randomUUID().toString()

        val result = mockMvc.post().uri("/api/v1/users/$nonExistentUserId/farms")
            .header("Authorization", "Bearer $token")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"cadastralReference":"9999999ZZ9999Z0004SA","cropTypes":["OLIVE"]}""")
            .exchange()

        assertThat(result)
            .hasStatus(400)
    }

    @Test
    fun `creating a farm with no crop types returns 400`() {
        val (userId, token) = registerAndLogIn()

        val result = mockMvc.post().uri("/api/v1/users/$userId/farms")
            .header("Authorization", "Bearer $token")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"cadastralReference":"9637302VG8173B0003SU","cropTypes":[]}""")
            .exchange()

        assertThat(result)
            .hasStatus(400)
    }

    @Test
    fun `listing farms for a user filtered by an existing crop type returns 200 with matching farms`() {
        val (userId, token) = registerAndLogIn()

        mockMvc.post().uri("/api/v1/users/$userId/farms")
            .header("Authorization", "Bearer $token")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"cadastralReference":"2234567AB1234C0005EH","cropTypes":["OLIVE"]}""")
            .exchange()

        val result = mockMvc.get().uri("/api/v1/users/$userId/farms?cropType=OLIVE")
            .header("Authorization", "Bearer $token")
            .contentType(MediaType.APPLICATION_JSON)
            .exchange()

        assertThat(result)
            .hasStatus(200)
            .bodyJson()
            .extractingPath("$.content")
            .asArray()
            .hasSize(1)
    }

    @Test
    fun `listing farms for a user filtered by an invalid crop type returns 400`() {
        val (userId, token) = registerAndLogIn()

        val result = mockMvc.get().uri("/api/v1/users/$userId/farms?cropType=INVALID")
            .header("Authorization", "Bearer $token")
            .contentType(MediaType.APPLICATION_JSON)
            .exchange()

        assertThat(result)
            .hasStatus(400)
    }

    @Test
    fun `listing farms for a user paginates the results`() {
        val (userId, token) = registerAndLogIn()

        listOf("1111167AB1234C0010FK", "2222267AB1234C0011KL", "3333367AB1234C0012XB").forEach { reference ->
            mockMvc.post().uri("/api/v1/users/$userId/farms")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"cadastralReference":"$reference","cropTypes":["OLIVE"]}""")
                .exchange()
        }

        val result = mockMvc.get().uri("/api/v1/users/$userId/farms?page=0&size=2")
            .header("Authorization", "Bearer $token")
            .contentType(MediaType.APPLICATION_JSON)
            .exchange()

        assertThat(result)
            .hasStatus(200)
            .bodyJson()
            .extractingPath("$.content").asArray().hasSize(2)
        assertThat(result)
            .bodyJson()
            .extractingPath("$.totalElements").isEqualTo(3)
        assertThat(result)
            .bodyJson()
            .extractingPath("$.totalPages").isEqualTo(2)
    }

    @Test
    fun `listing farms for a user sorted by cadastral reference descending returns them in that order`() {
        val (userId, token) = registerAndLogIn()

        listOf("4444467AB1234C0013EZ", "5555567AB1234C0014UX").forEach { reference ->
            mockMvc.post().uri("/api/v1/users/$userId/farms")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"cadastralReference":"$reference","cropTypes":["OLIVE"]}""")
                .exchange()
        }

        val result = mockMvc.get().uri("/api/v1/users/$userId/farms?sort=cadastralReference,desc")
            .header("Authorization", "Bearer $token")
            .contentType(MediaType.APPLICATION_JSON)
            .exchange()

        assertThat(result)
            .hasStatus(200)
            .bodyJson()
            .extractingPath("$.content[0].cadastralReference")
            .isEqualTo("5555567AB1234C0014UX")
    }

    @Test
    fun `listing farms for a user with an unsupported sort field returns 400`() {
        val (userId, token) = registerAndLogIn()

        val result = mockMvc.get().uri("/api/v1/users/$userId/farms?sort=unknown,asc")
            .header("Authorization", "Bearer $token")
            .contentType(MediaType.APPLICATION_JSON)
            .exchange()

        assertThat(result)
            .hasStatus(400)
    }

    @Test
    fun `listing farms for a user with no farms returns 200 with an empty list`() {
        val (userId, token) = registerAndLogIn()

        val result = mockMvc.get().uri("/api/v1/users/$userId/farms")
            .header("Authorization", "Bearer $token")
            .contentType(MediaType.APPLICATION_JSON)
            .exchange()

        assertThat(result)
            .hasStatus(200)
            .bodyJson()
            .extractingPath("$.content")
            .asArray()
            .hasSize(0)
    }

    @Test
    fun `listing farms without a token returns 401`() {
        val (userId, _) = registerAndLogIn()

        val result = mockMvc.get().uri("/api/v1/users/$userId/farms")
            .contentType(MediaType.APPLICATION_JSON)
            .exchange()

        assertThat(result)
            .hasStatus(401)
            .bodyJson()
            .extractingPath("$.message").isEqualTo("Full authentication is required to access this resource")
    }

    @Test
    fun `listing farms with an invalid token returns 401`() {
        val (userId, _) = registerAndLogIn()

        val result = mockMvc.get().uri("/api/v1/users/$userId/farms")
            .header("Authorization", "Bearer not-a-valid-token")
            .contentType(MediaType.APPLICATION_JSON)
            .exchange()

        assertThat(result)
            .hasStatus(401)
            .bodyJson()
            .extractingPath("$.message").isEqualTo("Full authentication is required to access this resource")
    }
}
