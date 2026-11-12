package com.fincaria

import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

@Testcontainers
abstract class PostgreSQLTestContainer {
    companion object {
        @JvmStatic
        @Container
        @ServiceConnection
        val postgres: PostgreSQLContainer =
            PostgreSQLContainer(
                DockerImageName.parse("postgis/postgis:18-3.7")
                    .asCompatibleSubstituteFor("postgres"),
            ).withCreateContainerCmdModifier { cmd -> cmd.withPlatform("linux/amd64") }
    }
}
