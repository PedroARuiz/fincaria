package com.fincaria

import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.testcontainers.postgresql.PostgreSQLContainer

abstract class PostgreSQLTestContainer {
    companion object {
        // Plain postgres for now (no migration uses PostGIS geometry yet) — it ships
        // a native arm64 image, unlike postgis/postgis, avoiding slow amd64
        // emulation on Apple Silicon. Switch back to postgis/postgis once a
        // migration actually needs geometry types.
        //
        // Not annotated with @Container: that ties the container's start/stop
        // lifecycle to each individual test CLASS via the JUnit5 Testcontainers
        // extension, even though this field is inherited by multiple test
        // classes — so the container gets stopped after the first class's tests
        // and a fresh one (new port) gets created for the next class, which
        // Spring's DataSource never manages to connect to. Starting it once here,
        // in the companion object's init block, makes it a true JVM-wide
        // singleton shared by every subclass; Ryuk (Testcontainers' resource
        // reaper) still cleans it up when the JVM exits.
        @JvmStatic
        @ServiceConnection
        val postgres: PostgreSQLContainer = PostgreSQLContainer("postgres:18")

        init {
            postgres.start()
        }
    }
}
