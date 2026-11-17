package com.fincaria.users.infrastructure.`in`.web

import com.fincaria.PostgreSQLTestContainer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.assertj.MockMvcTester
import org.springframework.web.context.WebApplicationContext

@SpringBootTest
class UserControllerTest(
    @Autowired context: WebApplicationContext,
) : PostgreSQLTestContainer() {
    private val mockMvc = MockMvcTester.from(context)

    @Test
    fun `creating a user with valid data returns 201 with the created user`() {
        val result = mockMvc.post().uri("/api/v1/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"name":"Pedro","email":"pedro@example.com"}""")
            .exchange()

        assertThat(result)
            .hasStatus(201)
            .bodyJson()
            .extractingPath("$.name").isEqualTo("Pedro")
    }

    @Test
    fun `creating a user with invalid data returns 400 with the validation error`() {
        val result = mockMvc.post().uri("/api/v1/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"name":" ","email":"pedro2@example.com"}""")
            .exchange()

        assertThat(result)
            .hasStatus(400)
            .bodyJson()
            .extractingPath("$.message").isEqualTo("name: must not be blank")
    }

    @Test
    fun `creating a user with an email already in use returns 409 with the conflict error`() {
        val requestBuilder = mockMvc.post().uri("/api/v1/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"name":"Pedro","email":"pedro3@example.com"}""")
        requestBuilder.exchange()

        val result = requestBuilder
            .exchange()

        assertThat(result)
            .hasStatus(409)
            .bodyJson()
            .extractingPath("$.message").isEqualTo("Email already in use: pedro3@example.com")
    }

}